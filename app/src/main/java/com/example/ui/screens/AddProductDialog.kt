package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductItem
import com.example.ui.components.BarcodeView
import com.example.ui.components.CameraBarcodeScannerDialog
import kotlin.random.Random

@Composable
fun AddProductDialog(
    initialBarcode: String = "",
    onDismiss: () -> Unit,
    onSaveProduct: (ProductItem) -> Unit
) {
    var barcode by remember {
        mutableStateOf(
            if (initialBarcode.isNotBlank()) initialBarcode
            else generateEan13()
        )
    }
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Alimentation") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCameraScanner by remember { mutableStateOf(false) }

    if (showCameraScanner) {
        CameraBarcodeScannerDialog(
            products = emptyList(),
            onBarcodeScanned = { scanned ->
                barcode = scanned
                showCameraScanner = false
            },
            onDismiss = { showCameraScanner = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_product_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (initialBarcode.isNotBlank()) "Enregistrer l'article scanné" else "Nouvel Article",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Renseignez le code-barres et le prix pour l'ajouter à votre catalogue de facturation.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Barcode input + generate button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = {
                            barcode = it.filter { ch -> ch.isLetterOrDigit() }
                            errorMessage = null
                        },
                        label = { Text("Code-barres *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_barcode")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedButton(
                        onClick = { showCameraScanner = true },
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("button_camera_scan_barcode")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scanner avec la caméra",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedButton(
                        onClick = { barcode = generateEan13() },
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("button_generate_barcode")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Générer un code-barres",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Visual barcode preview
                if (barcode.isNotBlank()) {
                    BarcodeView(
                        barcode = barcode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nom de l'article *") },
                    placeholder = { Text("ex. Bouteille d'eau 1.5L") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name")
                )

                // Price Input
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it.replace(',', '.')
                        errorMessage = null
                    },
                    label = { Text("Prix Unitaire (€) *") },
                    placeholder = { Text("ex. 2.50") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_price")
                )

                // Category Quick Select
                Text(
                    text = "Catégorie :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Alimentation", "Boissons", "Divers").forEach { cat ->
                        OutlinedButton(
                            onClick = { category = cat },
                            colors = if (category == cat) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Text(text = cat, fontSize = 11.sp)
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedBarcode = barcode.trim()
                    val trimmedName = name.trim()
                    val parsedPrice = priceText.toDoubleOrNull()

                    when {
                        trimmedBarcode.isEmpty() -> errorMessage = "Veuillez saisir un code-barres"
                        trimmedName.isEmpty() -> errorMessage = "Veuillez saisir le nom de l'article"
                        parsedPrice == null || parsedPrice < 0 -> errorMessage = "Veuillez saisir un prix valide"
                        else -> {
                            onSaveProduct(
                                ProductItem(
                                    barcode = trimmedBarcode,
                                    name = trimmedName,
                                    price = parsedPrice,
                                    category = category
                                )
                            )
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.testTag("button_save_product")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("button_cancel_product")
            ) {
                Text("Annuler")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

private fun generateEan13(): String {
    val randomDigits = (1..12).map { Random.nextInt(0, 10) }.joinToString("")
    // EAN-13 check digit calculation
    var sum = 0
    for (i in 0 until 12) {
        val digit = randomDigits[i].digitToInt()
        sum += if (i % 2 == 0) digit else digit * 3
    }
    val checkDigit = (10 - (sum % 10)) % 10
    return "$randomDigits$checkDigit"
}
