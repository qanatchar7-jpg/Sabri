package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ShoppingSessionWithItems(
    @Embedded val session: ShoppingSessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val scannedItems: List<ScannedItemRecord>
)
