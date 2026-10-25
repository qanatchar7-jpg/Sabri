package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scanned_items",
    indices = [Index(value = ["sessionId"]), Index(value = ["barcode"])]
)
data class ScannedItemRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val barcode: String,
    val productName: String,
    val price: Double,
    val quantity: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val scanSource: String = "CAMERAX" // CAMERAX, MANUEL, CATALOGUE
) {
    val lineTotal: Double
        get() = price * quantity
}
