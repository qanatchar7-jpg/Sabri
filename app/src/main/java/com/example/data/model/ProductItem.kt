package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductItem(
    @PrimaryKey val barcode: String,
    val name: String,
    val price: Double,
    val category: String = "Général",
    val description: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
