package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.ProductItem
import com.example.ui.sound.BarcodeSoundManager
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun CameraBarcodeScannerDialog(
    products: List<ProductItem>,
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var manualCodeInput by remember { mutableStateOf("") }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var hasScanned by remember { mutableStateOf(false) }

    // Toggle flash when flashEnabled changes
    LaunchedEffect(flashEnabled, cameraInstance) {
        cameraInstance?.cameraControl?.enableTorch(flashEnabled)
    }

    // Laser line animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
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
                .testTag("camera_barcode_scanner_dialog"),
            color = Color(0xFF090D16)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Bar
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
                            .testTag("close_camera_scanner_button")
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer le scanner",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Scanner CameraX",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (hasCameraPermission) "Détection automatique en direct" else "Caméra en attente d'autorisation",
                            color = if (hasCameraPermission) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { flashEnabled = !flashEnabled },
                        enabled = hasCameraPermission,
                        modifier = Modifier
                            .testTag("toggle_torch_button")
                            .clip(CircleShape)
                            .background(
                                if (flashEnabled) Color(0xFFF59E0B).copy(alpha = 0.35f)
                                else Color.White.copy(alpha = 0.15f)
                            )
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Torche",
                            tint = if (flashEnabled) Color(0xFFF59E0B) else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Camera Viewfinder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        // CameraX PreviewView with ML Kit Barcode Analyzer
                        CameraPreviewWithBarcodeScanner(
                            modifier = Modifier.fillMaxSize(),
                            onCameraReady = { cam -> cameraInstance = cam },
                            onBarcodeDetected = { barcode ->
                                if (!hasScanned) {
                                    hasScanned = true
                                    BarcodeSoundManager.playScanBeep()
                                    onBarcodeScanned(barcode)
                                    onDismiss()
                                }
                            }
                        )

                        // HUD Reticle and Animated Laser Line
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 4.dp.toPx()
                            val cornerLen = 32.dp.toPx()
                            val pad = 24.dp.toPx()
                            val cornerColor = Color(0xFF38BDF8)

                            // 4 Corner Brackets
                            drawLine(cornerColor, Offset(pad, pad), Offset(pad + cornerLen, pad), strokeWidth, StrokeCap.Round)
                            drawLine(cornerColor, Offset(pad, pad), Offset(pad, pad + cornerLen), strokeWidth, StrokeCap.Round)

                            drawLine(cornerColor, Offset(size.width - pad, pad), Offset(size.width - pad - cornerLen, pad), strokeWidth, StrokeCap.Round)
                            drawLine(cornerColor, Offset(size.width - pad, pad), Offset(size.width - pad, pad + cornerLen), strokeWidth, StrokeCap.Round)

                            drawLine(cornerColor, Offset(pad, size.height - pad), Offset(pad + cornerLen, size.height - pad), strokeWidth, StrokeCap.Round)
                            drawLine(cornerColor, Offset(pad, size.height - pad), Offset(pad, size.height - pad - cornerLen), strokeWidth, StrokeCap.Round)

                            drawLine(cornerColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - cornerLen, size.height - pad), strokeWidth, StrokeCap.Round)
                            drawLine(cornerColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - cornerLen), strokeWidth, StrokeCap.Round)

                            // Red laser line sweeping
                            val laserY = size.height * laserProgress
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFFEF4444),
                                        Color(0xFFFF3333),
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
                    } else {
                        // Permission Request Prompt
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Autorisation Caméra Requise",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Autorisez l'appareil photo pour scanner automatiquement les codes-barres avec CameraX.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.testTag("request_camera_permission_button")
                            ) {
                                Text("Activer la Caméra")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual barcode entry field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualCodeInput,
                        onValueChange = { manualCodeInput = it.filter { ch -> ch.isLetterOrDigit() } },
                        placeholder = { Text("Code-barres manuel", color = Color(0xFF64748B)) },
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
                            .testTag("camerax_manual_input")
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("camerax_confirm_manual_button")
                    ) {
                        Text("Valider")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Barcode Chips (Useful for emulator testing)
                Text(
                    text = "Codes-barres du catalogue (1-clic pour tester) :",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(products) { product ->
                        Card(
                            modifier = Modifier
                                .clickable {
                                    onBarcodeScanned(product.barcode)
                                    onDismiss()
                                }
                                .testTag("camera_quick_${product.barcode}"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = product.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${product.barcode} • ${String.format("%.2f", product.price)} €",
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp,
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

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithBarcodeScanner(
    modifier: Modifier = Modifier,
    onCameraReady: (Camera) -> Unit,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val rawVal = barcode.rawValue
                                    if (!rawVal.isNullOrBlank()) {
                                        onBarcodeDetected(rawVal)
                                        break
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    onCameraReady(camera)
                } catch (_: Exception) {
                    // Fallback to front camera or handle camera binding failure gracefully
                    try {
                        val frontSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            frontSelector,
                            preview,
                            imageAnalysis
                        )
                        onCameraReady(camera)
                    } catch (_: Exception) {
                        // Camera unavailable in environment
                    }
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}
