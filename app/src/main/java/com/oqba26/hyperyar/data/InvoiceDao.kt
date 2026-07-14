package com.oqba26.hyperyar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItem(item: InvoiceItem)

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Query("SELECT * FROM invoices WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getInvoicesInDateRange(start: Long, end: Long): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getInvoicesForCustomer(customerId: Int): Flow<List<Invoice>>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoice(invoiceId: Int): Flow<List<InvoiceItem>>
}
