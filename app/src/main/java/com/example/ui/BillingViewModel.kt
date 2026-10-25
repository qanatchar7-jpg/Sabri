package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.InvoiceRecord
import com.example.data.model.ProductItem
import com.example.data.model.ScannedItemRecord
import com.example.data.model.ShoppingSessionEntity
import com.example.data.model.ShoppingSessionWithItems
import com.example.data.repository.BillingRepository
import com.example.ui.sound.BarcodeSoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BillingViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repository = BillingRepository(
        productDao = db.productDao(),
        invoiceDao = db.invoiceDao(),
        sessionDao = db.shoppingSessionDao(),
        scannedItemDao = db.scannedItemDao()
    )

    val products: StateFlow<List<ProductItem>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceRecord>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRevenue: StateFlow<Double?> = repository.totalRevenue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val invoiceCount: StateFlow<Int> = repository.invoiceCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Previous Shopping Sessions from Room
    val shoppingSessions: StateFlow<List<ShoppingSessionWithItems>> = repository.allSessionsWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Timeline of all individually scanned items from Room
    val recentScannedItems: StateFlow<List<ScannedItemRecord>> = repository.recentScannedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active session ID
    private val _activeSessionId = MutableStateFlow(generateSessionId())
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    // Sound toggle state
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    fun toggleSound() {
        val newState = !_isSoundEnabled.value
        _isSoundEnabled.value = newState
        BarcodeSoundManager.isSoundEnabled = newState
        if (newState) {
            BarcodeSoundManager.playScanBeep()
        }
    }

    // Current Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Barcode input in the billing bar
    private val _barcodeInput = MutableStateFlow("")
    val barcodeInput: StateFlow<String> = _barcodeInput.asStateFlow()

    // Feedback message (e.g. "Article ajouté: X")
    private val _scanFeedback = MutableStateFlow<String?>(null)
    val scanFeedback: StateFlow<String?> = _scanFeedback.asStateFlow()

    // Trigger for dialog when an unknown barcode is scanned
    private val _unknownBarcode = MutableStateFlow<String?>(null)
    val unknownBarcode: StateFlow<String?> = _unknownBarcode.asStateFlow()

    // Last completed invoice to display receipt
    private val _completedInvoice = MutableStateFlow<InvoiceRecord?>(null)
    val completedInvoice: StateFlow<InvoiceRecord?> = _completedInvoice.asStateFlow()

    // Billing modifiers
    private val _discountPercent = MutableStateFlow(0.0)
    val discountPercent: StateFlow<Double> = _discountPercent.asStateFlow()

    private val _taxRate = MutableStateFlow(20.0) // Standard 20% TVA
    val taxRate: StateFlow<Double> = _taxRate.asStateFlow()

    // Computed totals
    val subtotal: StateFlow<Double> = _cartItems.combine(_discountPercent) { items, _ ->
        items.sumOf { it.lineTotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalItemsCount: StateFlow<Int> = _cartItems.combine(_discountPercent) { items, _ ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val grandTotal: StateFlow<Double> = combine(_cartItems, _discountPercent, _taxRate) { items, discount, tax ->
        val sub = items.sumOf { it.lineTotal }
        val discounted = sub * (1.0 - (discount / 100.0))
        val withTax = discounted * (1.0 + (tax / 100.0))
        if (withTax < 0.0) 0.0 else withTax
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onBarcodeInputChanged(code: String) {
        _barcodeInput.value = code
    }

    fun scanBarcode(code: String, source: String = "CAMERAX") {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            val product = repository.getProductByBarcode(trimmed)
            if (product != null) {
                addProductToCart(product)
                // Log scan in Room database history for current session
                repository.recordSingleScannedItem(
                    ScannedItemRecord(
                        sessionId = _activeSessionId.value,
                        barcode = product.barcode,
                        productName = product.name,
                        price = product.price,
                        quantity = 1,
                        timestamp = System.currentTimeMillis(),
                        scanSource = source
                    )
                )
                vibrateFeedback()
                BarcodeSoundManager.playScanBeep()
                _scanFeedback.value = "Scanné : ${product.name} (${String.format(Locale.FRANCE, "%.2f", product.price)} €)"
                _barcodeInput.value = ""
            } else {
                // Barcode not in catalog: Prompt user to save it with its price
                BarcodeSoundManager.playErrorBeep()
                _unknownBarcode.value = trimmed
                _barcodeInput.value = ""
            }
        }
    }

    fun dismissUnknownBarcodeDialog() {
        _unknownBarcode.value = null
    }

    fun clearFeedback() {
        _scanFeedback.value = null
    }

    fun addProductToCart(product: ProductItem) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.barcode == product.barcode }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(0, CartItem(product = product, quantity = 1))
        }
        _cartItems.value = current
    }

    fun updateItemQuantity(barcode: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.barcode == barcode }
        if (index >= 0) {
            val existing = current[index]
            val newQty = existing.quantity + delta
            if (newQty > 0) {
                current[index] = existing.copy(quantity = newQty)
            } else {
                current.removeAt(index)
            }
            _cartItems.value = current
        }
    }

    fun removeItem(barcode: String) {
        _cartItems.value = _cartItems.value.filterNot { it.product.barcode == barcode }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountPercent.value = 0.0
        _activeSessionId.value = generateSessionId()
    }

    fun setDiscount(percent: Double) {
        _discountPercent.value = percent
    }

    fun setTaxRate(rate: Double) {
        _taxRate.value = rate
    }

    fun saveProduct(product: ProductItem, autoAddToCart: Boolean = false) {
        viewModelScope.launch {
            repository.saveProduct(product)
            if (autoAddToCart) {
                addProductToCart(product)
                repository.recordSingleScannedItem(
                    ScannedItemRecord(
                        sessionId = _activeSessionId.value,
                        barcode = product.barcode,
                        productName = product.name,
                        price = product.price,
                        quantity = 1,
                        timestamp = System.currentTimeMillis(),
                        scanSource = "MANUEL"
                    )
                )
                _scanFeedback.value = "Article enregistré et ajouté : ${product.name}"
            }
        }
    }

    fun deleteProduct(product: ProductItem) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Save current cart as a named shopping session in Room
    fun saveShoppingSession(customName: String = "") {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE).format(Date(now))
        val sessionName = if (customName.isNotBlank()) customName else "Session du $dateStr"
        val total = grandTotal.value

        val sessionEntity = ShoppingSessionEntity(
            sessionId = _activeSessionId.value,
            sessionName = sessionName,
            startTime = now,
            endTime = now,
            status = "SAVED",
            totalItemsCount = totalItemsCount.value,
            totalAmount = total,
            note = "${items.size} articles distincts"
        )

        val scannedRecords = items.map {
            ScannedItemRecord(
                sessionId = _activeSessionId.value,
                barcode = it.product.barcode,
                productName = it.product.name,
                price = it.product.price,
                quantity = it.quantity,
                timestamp = now,
                scanSource = "SESSION_SAVED"
            )
        }

        viewModelScope.launch {
            repository.saveShoppingSession(sessionEntity, scannedRecords)
            _scanFeedback.value = "Session enregistrée : $sessionName"
            clearCart()
        }
    }

    // Resume / Load previous shopping session into active cart
    fun resumeSession(sessionWithItems: ShoppingSessionWithItems) {
        val newCart = sessionWithItems.scannedItems.map { scanned ->
            CartItem(
                product = ProductItem(
                    barcode = scanned.barcode,
                    name = scanned.productName,
                    price = scanned.price
                ),
                quantity = scanned.quantity
            )
        }
        _cartItems.value = newCart
        _activeSessionId.value = sessionWithItems.session.sessionId
        _scanFeedback.value = "Session reprise : ${sessionWithItems.session.sessionName}"
    }

    fun deleteSession(session: ShoppingSessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun checkout(paymentMethod: String, amountPaid: Double) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val now = System.currentTimeMillis()
        val dateCode = SimpleDateFormat("yyMMdd-HHmm", Locale.getDefault()).format(Date(now))
        val invoiceId = "FAC-$dateCode-${UUID.randomUUID().toString().take(4).uppercase()}"

        val itemsSummary = items.joinToString("; ") {
            "${it.product.name} (x${it.quantity} - ${String.format(Locale.FRANCE, "%.2f", it.lineTotal)}€)"
        }

        val sub = subtotal.value
        val disc = discountPercent.value
        val tax = taxRate.value
        val total = grandTotal.value
        val change = if (amountPaid > total) amountPaid - total else 0.0

        val record = InvoiceRecord(
            invoiceId = invoiceId,
            timestamp = now,
            itemsSummary = itemsSummary,
            totalItemsCount = totalItemsCount.value,
            subtotal = sub,
            discountPercent = disc,
            taxRate = tax,
            totalAmount = total,
            paymentMethod = paymentMethod,
            amountPaid = if (amountPaid > 0) amountPaid else total,
            changeDue = change
        )

        // Also record the shopping session as COMPLETED in Room
        val sessionEntity = ShoppingSessionEntity(
            sessionId = _activeSessionId.value,
            sessionName = "Facture $invoiceId",
            startTime = now,
            endTime = now,
            status = "COMPLETED",
            totalItemsCount = totalItemsCount.value,
            totalAmount = total,
            note = "Règlement en $paymentMethod"
        )

        val scannedRecords = items.map {
            ScannedItemRecord(
                sessionId = _activeSessionId.value,
                barcode = it.product.barcode,
                productName = it.product.name,
                price = it.product.price,
                quantity = it.quantity,
                timestamp = now,
                scanSource = "FACTURATION"
            )
        }

        viewModelScope.launch {
            repository.saveInvoice(record)
            repository.saveShoppingSession(sessionEntity, scannedRecords)
            BarcodeSoundManager.playSuccessPaymentChime()
            _completedInvoice.value = record
            clearCart()
        }
    }

    fun dismissCompletedInvoice() {
        _completedInvoice.value = null
    }

    fun deleteInvoice(invoice: InvoiceRecord) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    private fun generateSessionId(): String {
        val now = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        return "SES-$now"
    }

    private fun vibrateFeedback() {
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) {
            // Ignore on emulator
        }
    }
}
