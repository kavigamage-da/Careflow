package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillingDao {
    @Query("SELECT * FROM invoices ORDER BY created_at DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE status = 'PENDING' ORDER BY created_at DESC")
    fun getPendingInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE patient_id = :patientId ORDER BY created_at DESC")
    fun getInvoicesForPatient(patientId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT COUNT(*) FROM invoices WHERE status = 'PENDING'")
    fun getPendingInvoiceCount(): Flow<Int>

    @Query("SELECT SUM(paid_amount) FROM invoices WHERE status = 'PAID'")
    fun getTotalRevenue(): Flow<Double?>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE invoice_number = :invoiceNumber LIMIT 1")
    suspend fun getInvoiceByNumber(invoiceNumber: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)
}
