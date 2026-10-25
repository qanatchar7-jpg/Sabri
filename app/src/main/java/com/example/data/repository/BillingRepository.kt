package com.example.data.repository

import com.example.data.dao.InvoiceDao
import com.example.data.dao.ProductDao
import com.example.data.dao.ScannedItemDao
import com.example.data.dao.ShoppingSessionDao
import com.example.data.model.InvoiceRecord
import com.example.data.model.ProductItem
import com.example.data.model.ScannedItemRecord
import com.example.data.model.ShoppingSessionEntity
import com.example.data.model.ShoppingSessionWithItems
import kotlinx.coroutines.flow.Flow

class BillingRepository(
    private val productDao: ProductDao,
    private val invoiceDao: InvoiceDao,
    private val sessionDao: ShoppingSessionDao,
    private val scannedItemDao: ScannedItemDao
) {
    // Products
    val allProducts: Flow<List<ProductItem>> = productDao.getAllProducts()

    suspend fun getProductByBarcode(barcode: String): ProductItem? {
        return productDao.getProductByBarcode(barcode)
    }

    suspend fun saveProduct(product: ProductItem) {
        productDao.insertOrUpdate(product)
    }

    suspend fun deleteProduct(product: ProductItem) {
        productDao.delete(product)
    }

    // Invoices
    val allInvoices: Flow<List<InvoiceRecord>> = invoiceDao.getAllInvoices()
    val totalRevenue: Flow<Double?> = invoiceDao.getTotalRevenue()
    val invoiceCount: Flow<Int> = invoiceDao.getInvoiceCount()

    suspend fun saveInvoice(invoice: InvoiceRecord) {
        invoiceDao.insertInvoice(invoice)
    }

    suspend fun deleteInvoice(invoice: InvoiceRecord) {
        invoiceDao.deleteInvoice(invoice)
    }

    // Shopping Sessions & Scanned Items
    val allSessionsWithItems: Flow<List<ShoppingSessionWithItems>> = sessionDao.getAllSessionsWithItems()
    val recentScannedItems: Flow<List<ScannedItemRecord>> = scannedItemDao.getRecentScannedItems()
    val sessionCount: Flow<Int> = sessionDao.getSessionCount()

    suspend fun saveShoppingSession(
        session: ShoppingSessionEntity,
        items: List<ScannedItemRecord>
    ) {
        sessionDao.insertSession(session)
        if (items.isNotEmpty()) {
            scannedItemDao.deleteItemsForSession(session.sessionId)
            scannedItemDao.insertAll(items)
        }
    }

    suspend fun recordSingleScannedItem(item: ScannedItemRecord) {
        scannedItemDao.insertScannedItem(item)
    }

    suspend fun deleteSession(session: ShoppingSessionEntity) {
        scannedItemDao.deleteItemsForSession(session.sessionId)
        sessionDao.deleteSession(session)
    }
}
