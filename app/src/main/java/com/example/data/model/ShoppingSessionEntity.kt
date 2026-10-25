package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_sessions")
data class ShoppingSessionEntity(
    @PrimaryKey val sessionId: String,
    val sessionName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val status: String = "COMPLETED", // ACTIVE, COMPLETED, CANCELLED
    val totalItemsCount: Int = 0,
    val totalAmount: Double = 0.0,
    val note: String = ""
)
