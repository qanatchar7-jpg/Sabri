package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.InvoiceDao
import com.example.data.dao.ProductDao
import com.example.data.dao.ScannedItemDao
import com.example.data.dao.ShoppingSessionDao
import com.example.data.model.InvoiceRecord
import com.example.data.model.ProductItem
import com.example.data.model.ScannedItemRecord
import com.example.data.model.ShoppingSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductItem::class,
        InvoiceRecord::class,
        ShoppingSessionEntity::class,
        ScannedItemRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun shoppingSessionDao(): ShoppingSessionDao
    abstract fun scannedItemDao(): ScannedItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scan_facture_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            // Seed default products
                            database.productDao().insertAll(SampleData.defaultProducts)

                            // Seed an initial previous shopping session with scanned items
                            val initialSessionId = "SES-20261024-001"
                            val now = System.currentTimeMillis() - 3600_000 // 1 hour ago
                            val session = ShoppingSessionEntity(
                                sessionId = initialSessionId,
                                sessionName = "Courses Épicerie Matin",
                                startTime = now - 1800_000,
                                endTime = now,
                                status = "COMPLETED",
                                totalItemsCount = 3,
                                totalAmount = 6.45,
                                note = "Session enregistrée"
                            )
                            database.shoppingSessionDao().insertSession(session)

                            val scannedItems = listOf(
                                ScannedItemRecord(
                                    sessionId = initialSessionId,
                                    barcode = "3017620422003",
                                    productName = "Nutella Pâte à Tartiner 400g",
                                    price = 3.85,
                                    quantity = 1,
                                    timestamp = now - 1600_000,
                                    scanSource = "CAMERAX"
                                ),
                                ScannedItemRecord(
                                    sessionId = initialSessionId,
                                    barcode = "3228857000166",
                                    productName = "Eau Minérale Naturelle 1.5L",
                                    price = 0.95,
                                    quantity = 1,
                                    timestamp = now - 1400_000,
                                    scanSource = "CAMERAX"
                                ),
                                ScannedItemRecord(
                                    sessionId = initialSessionId,
                                    barcode = "7622210449283",
                                    productName = "Biscuits Petit Beurre Lu 200g",
                                    price = 1.75,
                                    quantity = 1,
                                    timestamp = now - 1200_000,
                                    scanSource = "MANUEL"
                                )
                            )
                            database.scannedItemDao().insertAll(scannedItems)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
