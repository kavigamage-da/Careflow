package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.security.SessionManager
import com.example.core.util.Resource
import com.example.data.local.CareFlowDatabase
import com.example.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID

class BillingRepositoryImpl(
    private val database: CareFlowDatabase,
    private val sessionManager: SessionManager,
    private val auditRepository: AuditRepository
) : BillingRepository {

    private val billingDao = database.billingDao()

    override fun getAllInvoices(): Flow<List<InvoiceEntity>> {
        return billingDao.getAllInvoices()
    }

    override fun getPendingInvoices(): Flow<List<InvoiceEntity>> {
        return billingDao.getPendingInvoices()
    }

    override fun getInvoicesForPatient(patientId: String): Flow<List<InvoiceEntity>> {
        return billingDao.getInvoicesForPatient(patientId)
    }

    override fun getTotalRevenue(): Flow<Double?> {
        return billingDao.getTotalRevenue()
    }

    override suspend fun getInvoiceById(id: String): InvoiceEntity? {
        return billingDao.getInvoiceById(id)
    }

    override suspend fun createInvoice(
        patientId: String,
        totalAmount: Double,
        itemsJson: String
    ): Resource<InvoiceEntity> {
        return try {
            val now = System.currentTimeMillis()
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val randomSuffix = (1000..9999).random()
            val invoiceNumber = "INV-$year-$randomSuffix"

            val invoice = InvoiceEntity(
                id = UUID.randomUUID().toString(),
                invoiceNumber = invoiceNumber,
                patientId = patientId,
                totalAmount = totalAmount,
                paidAmount = 0.0,
                status = "PENDING",
                itemsJson = itemsJson,
                createdAt = now
            )

            billingDao.insertInvoice(invoice)

            auditRepository.recordAction(
                action = AuditAction.INVOICE_GENERATED,
                entityName = "Invoice",
                entityId = invoice.id,
                details = "Invoice $invoiceNumber generated for patient $patientId. Total: $$totalAmount"
            )

            Resource.Success(invoice)
        } catch (e: Exception) {
            Resource.Error("Failed to create invoice: ${e.message}")
        }
    }

    override suspend fun processPayment(
        invoiceId: String,
        paidAmount: Double,
        paymentMethod: String
    ): Resource<InvoiceEntity> {
        return try {
            val invoice = billingDao.getInvoiceById(invoiceId)
                ?: return Resource.Error("Invoice not found: $invoiceId")

            if (invoice.status == "PAID") {
                return Resource.Error("Invoice ${invoice.invoiceNumber} is already fully settled.")
            }

            if (paidAmount <= 0.0) {
                return Resource.Error("Payment amount must be greater than zero.")
            }

            // Decimal-safe rounding to 2 places
            val roundedPayment = Math.round(paidAmount * 100.0) / 100.0
            val remainingBalance = Math.round((invoice.totalAmount - invoice.paidAmount) * 100.0) / 100.0

            if (roundedPayment > remainingBalance + 0.001) {
                return Resource.Error("Payment amount ($$roundedPayment) exceeds outstanding balance ($$remainingBalance).")
            }

            val now = System.currentTimeMillis()
            val newPaidTotal = Math.round((invoice.paidAmount + roundedPayment) * 100.0) / 100.0
            val isFullyPaid = newPaidTotal >= invoice.totalAmount - 0.001
            val newStatus = if (isFullyPaid) "PAID" else "PARTIALLY_PAID"

            val updated = invoice.copy(
                paidAmount = newPaidTotal,
                status = newStatus,
                paymentMethod = paymentMethod,
                paidAt = if (isFullyPaid) now else invoice.paidAt
            )

            billingDao.updateInvoice(updated)

            auditRepository.recordAction(
                action = AuditAction.PAYMENT_RECORDED,
                entityName = "Invoice",
                entityId = invoiceId,
                details = "Payment of $$roundedPayment recorded via $paymentMethod for invoice ${invoice.invoiceNumber}. Status: $newStatus"
            )

            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to process payment: ${e.message}")
        }
    }
}
