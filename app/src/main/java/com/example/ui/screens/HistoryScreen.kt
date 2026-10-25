package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceRecord
import com.example.data.model.ScannedItemRecord
import com.example.data.model.ShoppingSessionWithItems
import com.example.ui.BillingViewModel
import com.example.ui.components.BarcodeView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: BillingViewModel,
    onNavigateToBilling: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val invoices by viewModel.invoices.collectAsState()
    val shoppingSessions by viewModel.shoppingSessions.collectAsState()
    val recentScannedItems by viewModel.recentScannedItems.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val invoiceCount by viewModel.invoiceCount.collectAsState()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedInvoiceForReceipt by remember { mutableStateOf<InvoiceRecord?>(null) }

    val revenue = totalRevenue ?: 0.0
    val averageBasket = if (invoiceCount > 0) revenue / invoiceCount else 0.0

    if (selectedInvoiceForReceipt != null) {
        ReceiptDialog(
            invoice = selectedInvoiceForReceipt!!,
            onDismiss = { selectedInvoiceForReceipt = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
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
                Text(
                    text = "Historique & Sessions de Courses",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Base Room locale des sessions et articles scannés",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mini Metrics Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "Sessions",
                        value = "${shoppingSessions.size}",
                        icon = Icons.Default.ShoppingCart,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Total Scans",
                        value = "${recentScannedItems.size}",
                        icon = Icons.Default.QrCodeScanner,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Facturé",
                        value = "${String.format(Locale.FRANCE, "%.2f", revenue)} €",
                        icon = Icons.Default.Paid,
                        tint = Color(0xFF059669),
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Sessions (${shoppingSessions.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_history_sessions")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Scans (${recentScannedItems.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_history_scans")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Factures (${invoices.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_history_invoices")
                    )
                }
            }
        }

        // Content according to selected sub-tab
        when (selectedTab) {
            0 -> ShoppingSessionsListView(
                sessions = shoppingSessions,
                onResumeSession = { session ->
                    viewModel.resumeSession(session)
                    onNavigateToBilling?.invoke()
                },
                onDeleteSession = { session ->
                    viewModel.deleteSession(session.session)
                }
            )
            1 -> ScannedItemsHistoryView(items = recentScannedItems)
            2 -> InvoicesHistoryView(
                invoices = invoices,
                onSelectInvoice = { selectedInvoiceForReceipt = it },
                onDeleteInvoice = { viewModel.deleteInvoice(it) }
            )
        }
    }
}

@Composable
fun ShoppingSessionsListView(
    sessions: List<ShoppingSessionWithItems>,
    onResumeSession: (ShoppingSessionWithItems) -> Unit,
    onDeleteSession: (ShoppingSessionWithItems) -> Unit
) {
    if (sessions.isEmpty()) {
        EmptyHistoryState(
            icon = Icons.Default.ShoppingCart,
            title = "Aucune session d'achat",
            description = "Vos sessions de courses avec articles scannés apparaîtront ici."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions, key = { it.session.sessionId }) { sessionWithItems ->
                ShoppingSessionCard(
                    sessionWithItems = sessionWithItems,
                    onResume = { onResumeSession(sessionWithItems) },
                    onDelete = { onDeleteSession(sessionWithItems) }
                )
            }
        }
    }
}

@Composable
fun ShoppingSessionCard(
    sessionWithItems: ShoppingSessionWithItems,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    val formattedDate = remember(sessionWithItems.session.startTime) {
        dateFormat.format(Date(sessionWithItems.session.startTime))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_card_${sessionWithItems.session.sessionId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sessionWithItems.session.sessionName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$formattedDate • ${sessionWithItems.session.sessionId}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (sessionWithItems.session.status == "COMPLETED") Color(0xFFD1FAE5) else Color(0xFFE0F2FE)
                ) {
                    Text(
                        text = if (sessionWithItems.session.status == "COMPLETED") "Encaissée" else "Enregistrée",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sessionWithItems.session.status == "COMPLETED") Color(0xFF047857) else Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${sessionWithItems.scannedItems.sumOf { it.quantity }} article(s) scanné(s)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format(Locale.FRANCE, "%.2f", sessionWithItems.session.totalAmount)} €",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF059669)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand / Collapse detailed scanned items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Masquer le détail" else "Voir les articles scannés (${sessionWithItems.scannedItems.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expanded Item List
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    sessionWithItems.scannedItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${item.barcode} • Qté: ${item.quantity} (via ${item.scanSource})",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${String.format(Locale.FRANCE, "%.2f", item.lineTotal)} €",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions: Resume Session in Cart & Delete Session
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer la session",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("resume_session_button_${sessionWithItems.session.sessionId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Charger dans le panier",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ScannedItemsHistoryView(items: List<ScannedItemRecord>) {
    if (items.isEmpty()) {
        EmptyHistoryState(
            icon = Icons.Default.QrCodeScanner,
            title = "Aucun scan enregistré",
            description = "Chaque article scanné par la caméra ou en saisie directe sera historisé ici."
        )
    } else {
        val timeFormat = remember { SimpleDateFormat("HH:mm:ss - dd/MM", Locale.FRANCE) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (item.scanSource == "CAMERAX") Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.scanSource == "CAMERAX") Icons.Default.CameraAlt else Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = if (item.scanSource == "CAMERAX") Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${item.barcode} • ${timeFormat.format(Date(item.timestamp))}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format(Locale.FRANCE, "%.2f", item.price)} €",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF059669)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = item.scanSource,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoicesHistoryView(
    invoices: List<InvoiceRecord>,
    onSelectInvoice: (InvoiceRecord) -> Unit,
    onDeleteInvoice: (InvoiceRecord) -> Unit
) {
    if (invoices.isEmpty()) {
        EmptyHistoryState(
            icon = Icons.Default.ReceiptLong,
            title = "Aucune facture encaissée",
            description = "Les factures validées apparaîtront ici avec leurs tickets de caisse."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(invoices, key = { it.invoiceId }) { invoice ->
                InvoiceHistoryCard(
                    invoice = invoice,
                    onClick = { onSelectInvoice(invoice) },
                    onDelete = { onDeleteInvoice(invoice) }
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryState(
    icon: ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InvoiceHistoryCard(
    invoice: InvoiceRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    val formattedDate = remember(invoice.timestamp) { dateFormat.format(Date(invoice.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_card_${invoice.invoiceId}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = invoice.invoiceId,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = invoice.paymentMethod,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${invoice.totalItemsCount} article(s) • ${invoice.itemsSummary.take(45)}...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Total Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format(Locale.FRANCE, "%.2f", invoice.totalAmount)} €",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = Color(0xFF059669)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer la facture",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Voir le reçu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
