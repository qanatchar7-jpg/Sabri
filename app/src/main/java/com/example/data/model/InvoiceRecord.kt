package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceRecord(
    @PrimaryKey val invoiceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val itemsSummary: String,
    val totalItemsCount: Int,
    val subtotal: Double,
    val discountPercent: Double = 0.0,
    val taxRate: Double = 20.0,
    val totalAmount: Double,
    val paymentMethod: String = "Espèces",
    val amountPaid: Double = 0.0,
    val changeDue: Double = 0.0
)
