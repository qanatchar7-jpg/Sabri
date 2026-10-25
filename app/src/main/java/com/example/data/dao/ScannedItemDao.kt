package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ScannedItemRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedItem(item: ScannedItemRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ScannedItemRecord>)

    @Query("SELECT * FROM scanned_items WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getItemsForSession(sessionId: String): Flow<List<ScannedItemRecord>>

    @Query("SELECT * FROM scanned_items ORDER BY timestamp DESC LIMIT 100")
    fun getRecentScannedItems(): Flow<List<ScannedItemRecord>>

    @Query("DELETE FROM scanned_items WHERE sessionId = :sessionId")
    suspend fun deleteItemsForSession(sessionId: String)

    @Delete
    suspend fun deleteScannedItem(item: ScannedItemRecord)

    @Query("SELECT COUNT(*) FROM scanned_items")
    fun getTotalScannedCount(): Flow<Int>
}
