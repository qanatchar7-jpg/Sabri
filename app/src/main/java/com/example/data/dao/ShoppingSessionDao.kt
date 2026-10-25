package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ShoppingSessionEntity
import com.example.data.model.ShoppingSessionWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingSessionDao {
    @Transaction
    @Query("SELECT * FROM shopping_sessions ORDER BY startTime DESC")
    fun getAllSessionsWithItems(): Flow<List<ShoppingSessionWithItems>>

    @Transaction
    @Query("SELECT * FROM shopping_sessions WHERE sessionId = :id LIMIT 1")
    suspend fun getSessionWithItemsById(id: String): ShoppingSessionWithItems?

    @Query("SELECT * FROM shopping_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ShoppingSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ShoppingSessionEntity)

    @Update
    suspend fun updateSession(session: ShoppingSessionEntity)

    @Delete
    suspend fun deleteSession(session: ShoppingSessionEntity)

    @Query("DELETE FROM shopping_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("SELECT COUNT(*) FROM shopping_sessions")
    fun getSessionCount(): Flow<Int>
}
