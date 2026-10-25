package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun getAllProducts(): Flow<List<ProductItem>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductItem?

    @Query("SELECT * FROM products WHERE barcode LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(product: ProductItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductItem>)

    @Update
    suspend fun update(product: ProductItem)

    @Delete
    suspend fun delete(product: ProductItem)

    @Query("DELETE FROM products WHERE barcode = :barcode")
    suspend fun deleteByBarcode(barcode: String)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
