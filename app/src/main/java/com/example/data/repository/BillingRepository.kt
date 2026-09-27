package com.example.data.repository

import com.example.core.util.Resource
import com.example.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    fun getAllInvoices(): Flow<List<InvoiceEntity>>
    fun getPendingInvoices(): Flow<List<InvoiceEntity>>
    fun getInvoicesForPatient(patientId: String): Flow<List<InvoiceEntity>>
    fun getTotalRevenue(): Flow<Double?>
    suspend fun getInvoiceById(id: String): InvoiceEntity?
    suspend fun createInvoice(patientId: String, totalAmount: Double, itemsJson: String): Resource<InvoiceEntity>
    suspend fun processPayment(invoiceId: String, paidAmount: Double, paymentMethod: String): Resource<InvoiceEntity>
}
