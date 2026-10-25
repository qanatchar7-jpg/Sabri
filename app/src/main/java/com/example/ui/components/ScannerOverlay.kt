package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProductItem

@Composable
fun BarcodeScannerDialog(
    products: List<ProductItem>,
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var manualCodeInput by remember { mutableStateOf("") }
    var flashEnabled by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserProgress"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("barcode_scanner_dialog"),
            color = Color(0xFF090D16)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("close_scanner_button")
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer le scanner",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Scanner de Code-Barres",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    IconButton(
                        onClick = { flashEnabled = !flashEnabled },
                        modifier = Modifier
                            .testTag("toggle_flash_button")
                            .clip(CircleShape)
                            .background(
                                if (flashEnabled) Color(0xFFF59E0B).copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.12f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashlightOn,
                            contentDescription = "Torche",
                            tint = if (flashEnabled) Color(0xFFF59E0B) else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cadrez le code-barres dans la zone de lecture ou choisissez un article rapide ci-dessous",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Scanner Viewfinder Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (flashEnabled) Color(0xFF1E293B) else Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Viewfinder Corners & Red Laser Line
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 5.dp.toPx()
                        val cornerLen = 36.dp.toPx()
                        val pad = 24.dp.toPx()
                        val cornerColor = Color(0xFF0284C7)

                        // Top-Left corner
                        drawLine(cornerColor, Offset(pad, pad), Offset(pad + cornerLen, pad), strokeWidth, StrokeCap.Round)
                        drawLine(cornerColor, Offset(pad, pad), Offset(pad, pad + cornerLen), strokeWidth, StrokeCap.Round)

                        // Top-Right corner
                        drawLine(cornerColor, Offset(size.width - pad, pad), Offset(size.width - pad - cornerLen, pad), strokeWidth, StrokeCap.Round)
                        drawLine(cornerColor, Offset(size.width - pad, pad), Offset(size.width - pad, pad + cornerLen), strokeWidth, StrokeCap.Round)

                        // Bottom-Left corner
                        drawLine(cornerColor, Offset(pad, size.height - pad), Offset(pad + cornerLen, size.height - pad), strokeWidth, StrokeCap.Round)
                        drawLine(cornerColor, Offset(pad, size.height - pad), Offset(pad, size.height - pad - cornerLen), strokeWidth, StrokeCap.Round)

                        // Bottom-Right corner
                        drawLine(cornerColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - cornerLen, size.height - pad), strokeWidth, StrokeCap.Round)
                        drawLine(cornerColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - cornerLen), strokeWidth, StrokeCap.Round)

                        // Laser sweep line
                        val laserY = size.height * laserProgress
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFEF4444),
                                    Color(0xFFFF2222),
                                    Color(0xFFEF4444),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(pad, laserY),
                            end = Offset(size.width - pad, laserY),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Capteur optique actif",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Manual Input in Scanner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualCodeInput,
                        onValueChange = { manualCodeInput = it.filter { ch -> ch.isLetterOrDigit() } },
                        placeholder = { Text("Ou saisissez le code-barres", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scanner_manual_code_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (manualCodeInput.isNotBlank()) {
                                onBarcodeScanned(manualCodeInput.trim())
                                onDismiss()
                            }
                        },
                        enabled = manualCodeInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("scanner_confirm_code_button")
                    ) {
                        Text("Valider")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Barcode Presets (Tap to simulate instant scan)
                Text(
                    text = "Scan Rapide (Articles Enregistrés) :",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(products) { product ->
                        Card(
                            modifier = Modifier
                                .clickable {
                                    onBarcodeScanned(product.barcode)
                                    onDismiss()
                                }
                                .testTag("quick_scan_${product.barcode}"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = product.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = product.barcode,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${String.format("%.2f", product.price)} €",
                                        color = Color(0xFF10B981),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
