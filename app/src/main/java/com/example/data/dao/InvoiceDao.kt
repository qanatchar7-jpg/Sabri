package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InvoiceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<InvoiceRecord>>

    @Query("SELECT * FROM invoices WHERE invoiceId = :id LIMIT 1")
    suspend fun getInvoiceById(id: String): InvoiceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceRecord)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceRecord)

    @Query("SELECT SUM(totalAmount) FROM invoices")
    fun getTotalRevenue(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM invoices")
    fun getInvoiceCount(): Flow<Int>
}
