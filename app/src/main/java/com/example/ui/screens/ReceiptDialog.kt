package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InvoiceRecord
import com.example.ui.components.BarcodeView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptDialog(
    invoice: InvoiceRecord,
    onDismiss: () -> Unit,
    onNewSale: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
    val formattedDate = dateFormat.format(Date(invoice.timestamp))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("receipt_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Facture validée",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Facture Encaissée",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "N° ${invoice.invoiceId}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Slip Card (Stylized Thermal Paper Look)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "DÉTAIL DES ARTICLES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Items summary lines
                        val items = invoice.itemsSummary.split(";")
                        for (itemStr in items) {
                            if (itemStr.isNotBlank()) {
                                Text(
                                    text = itemStr.trim(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = Color(0xFFCBD5E1)
                        )

                        // Subtotal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sous-total :", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text("${String.format(Locale.FRANCE, "%.2f", invoice.subtotal)} €", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Tax rate
                        if (invoice.taxRate > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TVA (${String.format(Locale.FRANCE, "%.1f", invoice.taxRate)}%) :", fontSize = 13.sp, color = Color(0xFF64748B))
                                val taxAmount = invoice.subtotal * (invoice.taxRate / 100.0)
                                Text("${String.format(Locale.FRANCE, "%.2f", taxAmount)} €", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Discount if any
                        if (invoice.discountPercent > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remise (${invoice.discountPercent}%) :", fontSize = 13.sp, color = Color(0xFFEF4444))
                                val disc = invoice.subtotal * (invoice.discountPercent / 100.0)
                                Text("-${String.format(Locale.FRANCE, "%.2f", disc)} €", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFF0F172A),
                            thickness = 1.5.dp
                        )

                        // Grand Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TOTAL TTC :",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                "${String.format(Locale.FRANCE, "%.2f", invoice.totalAmount)} €",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0284C7)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Payment info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Règlement :", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(invoice.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (invoice.paymentMethod == "Espèces" && invoice.amountPaid > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Reçu :", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("${String.format(Locale.FRANCE, "%.2f", invoice.amountPaid)} €", fontSize = 12.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Rendu de monnaie :", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                Text("${String.format(Locale.FRANCE, "%.2f", invoice.changeDue)} €", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Barcode representation of invoice ID
                BarcodeView(
                    barcode = invoice.invoiceId.filter { it.isDigit() }.ifEmpty { "20260012" },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ScanFacture v1.0 • Dev: O.SABRI • 24/10/2026",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareReceipt(context, invoice, formattedDate)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_receipt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Partager",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Partager")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onNewSale?.invoke()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("close_receipt_button")
                    ) {
                        Text("Terminer")
                    }
                }
            }
        }
    }
}

private fun shareReceipt(context: Context, invoice: InvoiceRecord, formattedDate: String) {
    val text = buildString {
        appendLine("═══════════════════════════════")
        appendLine("       SCANFACTURE - REÇU      ")
        appendLine("═══════════════════════════════")
        appendLine("Facture : ${invoice.invoiceId}")
        appendLine("Date : $formattedDate")
        appendLine("Mode de paiement : ${invoice.paymentMethod}")
        appendLine("-------------------------------")
        appendLine("ARTICLES :")
        for (item in invoice.itemsSummary.split(";")) {
            if (item.isNotBlank()) {
                appendLine(" • ${item.trim()}")
            }
        }
        appendLine("-------------------------------")
        appendLine("Sous-total : ${String.format(Locale.FRANCE, "%.2f", invoice.subtotal)} €")
        if (invoice.taxRate > 0) {
            val taxAmount = invoice.subtotal * (invoice.taxRate / 100.0)
            appendLine("TVA (${invoice.taxRate}%) : ${String.format(Locale.FRANCE, "%.2f", taxAmount)} €")
        }
        appendLine("TOTAL TTC : ${String.format(Locale.FRANCE, "%.2f", invoice.totalAmount)} €")
        if (invoice.amountPaid > 0) {
            appendLine("Reçu : ${String.format(Locale.FRANCE, "%.2f", invoice.amountPaid)} €")
            appendLine("Rendu : ${String.format(Locale.FRANCE, "%.2f", invoice.changeDue)} €")
        }
        appendLine("═══════════════════════════════")
        appendLine("Merci pour votre visite !")
        appendLine("ScanFacture v1.0 • Dev: O.SABRI (24/10/2026)")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Facture ${invoice.invoiceId}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Partager la facture"))
}
