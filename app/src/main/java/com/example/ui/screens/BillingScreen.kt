package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.ui.BillingViewModel
import com.example.ui.components.CameraBarcodeScannerDialog
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun BillingScreen(
    viewModel: BillingViewModel,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val products by viewModel.products.collectAsState()
    val barcodeInput by viewModel.barcodeInput.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()
    val totalItemsCount by viewModel.totalItemsCount.collectAsState()
    val discountPercent by viewModel.discountPercent.collectAsState()
    val taxRate by viewModel.taxRate.collectAsState()
    val scanFeedback by viewModel.scanFeedback.collectAsState()
    val unknownBarcode by viewModel.unknownBarcode.collectAsState()
    val completedInvoice by viewModel.completedInvoice.collectAsState()

    var showScannerDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // Auto-dismiss feedback banner after 3 seconds
    LaunchedEffect(scanFeedback) {
        if (scanFeedback != null) {
            delay(3000)
            viewModel.clearFeedback()
        }
    }

    // Modal when unknown barcode is entered
    if (unknownBarcode != null) {
        AddProductDialog(
            initialBarcode = unknownBarcode!!,
            onDismiss = { viewModel.dismissUnknownBarcodeDialog() },
            onSaveProduct = { product ->
                viewModel.saveProduct(product, autoAddToCart = true)
                viewModel.dismissUnknownBarcodeDialog()
            }
        )
    }

    // Modal for completed invoice receipt
    if (completedInvoice != null) {
        ReceiptDialog(
            invoice = completedInvoice!!,
            onDismiss = { viewModel.dismissCompletedInvoice() },
            onNewSale = { viewModel.dismissCompletedInvoice() }
        )
    }

    // CameraX Barcode Scanner Modal
    if (showScannerDialog) {
        CameraBarcodeScannerDialog(
            products = products,
            onBarcodeScanned = { code ->
                viewModel.scanBarcode(code)
            },
            onDismiss = { showScannerDialog = false }
        )
    }

    // Checkout / Payment Dialog
    if (showCheckoutDialog) {
        CheckoutDialog(
            totalAmount = grandTotal,
            onDismiss = { showCheckoutDialog = false },
            onConfirmPayment = { method, amountPaid ->
                viewModel.checkout(method, amountPaid)
                showCheckoutDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Facturation Directe",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Collectez et calculez les montants par code-barres",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Ticket items counter badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.testTag("cart_items_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalItemsCount art.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Barcode Input Field + Camera Scanner Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { viewModel.onBarcodeInputChanged(it) },
                        placeholder = { Text("Code-barres ou réf...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (barcodeInput.isNotBlank()) {
                                    viewModel.scanBarcode(barcodeInput)
                                }
                            }
                        ),
                        trailingIcon = {
                            if (barcodeInput.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onBarcodeInputChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Effacer",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("barcode_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Scanner button
                    IconButton(
                        onClick = { showScannerDialog = true },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .testTag("open_scanner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Ouvrir le scanner",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Add Button
                    Button(
                        onClick = {
                            if (barcodeInput.isNotBlank()) {
                                viewModel.scanBarcode(barcodeInput)
                            }
                        },
                        enabled = barcodeInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("add_barcode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter à la facture"
                        )
                    }
                }

                // Quick Barcode Test Chips
                if (products.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Articles rapides (toucher pour biper) :",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 2.dp)
                    ) {
                        items(products.take(6)) { prod ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { viewModel.scanBarcode(prod.barcode) }
                                    .testTag("quick_chip_${prod.barcode}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = prod.name.take(16),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${String.format(Locale.FRANCE, "%.2f", prod.price)} €",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Animated Scan Feedback Banner
        AnimatedVisibility(
            visible = scanFeedback != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF10B981))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("scan_feedback_banner")
            ) {
                Text(
                    text = scanFeedback ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Cart List or Empty State
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Facture en attente de code-barres",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Saisissez ou scannez un code-barres ci-dessus pour ajouter des articles et calculer le montant total en temps réel.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cartItems, key = { it.product.barcode }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrement = { viewModel.updateItemQuantity(item.product.barcode, 1) },
                        onDecrement = { viewModel.updateItemQuantity(item.product.barcode, -1) },
                        onRemove = { viewModel.removeItem(item.product.barcode) }
                    )
                }
            }
        }

        // Sticky Bottom Billing Summary & Actions
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // TVA & Remise Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TVA :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        listOf(0.0, 5.5, 20.0).forEach { rate ->
                            FilterChip(
                                selected = taxRate == rate,
                                onClick = { viewModel.setTaxRate(rate) },
                                label = { Text("${rate.toInt()}%", fontSize = 11.sp) },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .height(32.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Remise :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        listOf(0.0, 10.0).forEach { disc ->
                            FilterChip(
                                selected = discountPercent == disc,
                                onClick = { viewModel.setDiscount(disc) },
                                label = { Text("${disc.toInt()}%", fontSize = 11.sp) },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .height(32.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Totals Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Sous-total : ${String.format(Locale.FRANCE, "%.2f", subtotal)} €",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (discountPercent > 0) {
                            Text(
                                text = "Remise appliquée : -${discountPercent.toInt()}%",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "MONTANT TOTAL",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Grand Total Number
                    Text(
                        text = "${String.format(Locale.FRANCE, "%.2f", grandTotal)} €",
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("grand_total_text")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Clear Cart & Checkout Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (cartItems.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.saveShoppingSession() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("save_session_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = "Enregistrer la session",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearCart() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("clear_cart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Vider la facture",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Button(
                        onClick = { showCheckoutDialog = true },
                        enabled = cartItems.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("checkout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Encaisser (${String.format(Locale.FRANCE, "%.2f", grandTotal)} €)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.product.barcode}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.product.barcode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${String.format(Locale.FRANCE, "%.2f", item.product.price)} € / u",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quantity Stepper: [-] qty [+]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                        contentDescription = "Diminuer",
                        modifier = Modifier.size(16.dp),
                        tint = if (item.quantity == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Augmenter",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Line Total Price
            Text(
                text = "${String.format(Locale.FRANCE, "%.2f", item.lineTotal)} €",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.width(68.dp)
            )
        }
    }
}

@Composable
fun CheckoutDialog(
    totalAmount: Double,
    onDismiss: () -> Unit,
    onConfirmPayment: (paymentMethod: String, amountPaid: Double) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Espèces") }
    var receivedAmountText by remember {
        mutableStateOf(String.format(Locale.US, "%.2f", totalAmount))
    }
    val receivedAmount = receivedAmountText.toDoubleOrNull() ?: totalAmount
    val changeDue = if (receivedAmount > totalAmount) receivedAmount - totalAmount else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("checkout_dialog"),
        title = {
            Text(
                text = "Règlement de la facture",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Total Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL À ENCAISSER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${String.format(Locale.FRANCE, "%.2f", totalAmount)} €",
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Payment Method Selector
                Text(
                    text = "Mode de paiement :",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Espèces", "Carte", "Virement").forEach { method ->
                        OutlinedButton(
                            onClick = { selectedMethod = method },
                            colors = if (selectedMethod == method) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            } else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = method, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Cash Calculation
                if (selectedMethod == "Espèces") {
                    OutlinedTextField(
                        value = receivedAmountText,
                        onValueChange = { receivedAmountText = it.replace(',', '.') },
                        label = { Text("Montant reçu (€)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("received_amount_input")
                    )

                    // Quick Cash Buttons (Exact, 10, 20, 50)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Exact" to totalAmount,
                            "10€" to 10.0,
                            "20€" to 20.0,
                            "50€" to 50.0
                        ).forEach { (label, value) ->
                            if (value >= totalAmount || label == "Exact") {
                                OutlinedButton(
                                    onClick = {
                                        receivedAmountText = String.format(Locale.US, "%.2f", value)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(label, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Change due display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rendu monnaie :",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${String.format(Locale.FRANCE, "%.2f", changeDue)} €",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF047857),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmPayment(selectedMethod, receivedAmount)
                },
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Valider & Émettre le reçu")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
