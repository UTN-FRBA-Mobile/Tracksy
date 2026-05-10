package com.example.tracksy.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.tracksy.ui.theme.TracksyBackground
import com.example.tracksy.ui.theme.TracksyPrimary
import com.example.tracksy.ui.theme.TracksyQrBorder
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

// Punto de entrada
@Composable
fun BarcodeScannerScreen(
    onBarcodeDetected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (hasPermission) {
        ScannerView(onBarcodeDetected = onBarcodeDetected, onDismiss = onDismiss)
    } else {
        PermissionDeniedView(onDismiss = onDismiss)
    }
}

@Composable
private fun ScannerView(
    onBarcodeDetected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasScanned     = remember { AtomicBoolean(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Camara preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { proxy ->
                        if (!hasScanned.get()) {
                            analyzeProxy(scanner, proxy) { rawValue ->
                                if (hasScanned.compareAndSet(false, true)) {
                                    onBarcodeDetected(rawValue)
                                }
                            }
                        } else {
                            proxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        ScannerOverlay(scanLineProgress = scanLineProgress)

        // Encabezado con botón de cierre
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onDismiss)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Close,
                    contentDescription = "Cerrar",
                    tint               = Color.White,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 60.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.QrCodeScanner,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text      = "Escaneá el código de barras\ndel producto",
                color     = Color.White,
                fontSize  = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ScannerOverlay(scanLineProgress: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cutW   = size.width * 0.72f
        val cutH   = cutW * 0.62f
        val left   = (size.width  - cutW) / 2f
        val top    = (size.height - cutH) / 2f - size.height * 0.04f
        val radius = 24.dp.toPx()

        val cutoutPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect         = Rect(left, top, left + cutW, top + cutH),
                    cornerRadius = CornerRadius(radius)
                )
            )
        }

        clipPath(cutoutPath, clipOp = ClipOp.Difference) {
            drawRect(color = Color.Black.copy(alpha = 0.62f))
        }

        drawRoundRect(
            color       = TracksyQrBorder,
            topLeft     = Offset(left, top),
            size        = Size(cutW, cutH),
            cornerRadius = CornerRadius(radius),
            style       = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        val bracketLen  = 24.dp.toPx()
        val strokeW     = 3.dp.toPx()
        val corners = listOf(
            Offset(left, top) to Pair(1f, 1f),
            Offset(left + cutW, top) to Pair(-1f, 1f),
            Offset(left, top + cutH) to Pair(1f, -1f),
            Offset(left + cutW, top + cutH) to Pair(-1f, -1f)
        )
        corners.forEach { (corner, dir) ->
            drawLine(Color.White, corner, Offset(corner.x + bracketLen * dir.first, corner.y), strokeW)
            drawLine(Color.White, corner, Offset(corner.x, corner.y + bracketLen * dir.second), strokeW)
        }

        val lineY = top + cutH * scanLineProgress
        if (lineY in top..(top + cutH)) {
            clipPath(cutoutPath) {
                drawLine(
                    color       = TracksyPrimary.copy(alpha = 0.85f),
                    start       = Offset(left + 4.dp.toPx(), lineY),
                    end         = Offset(left + cutW - 4.dp.toPx(), lineY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun analyzeProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    proxy: ImageProxy,
    onResult: (String) -> Unit
) {
    val mediaImage = proxy.image
    if (mediaImage == null) {
        proxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull()?.rawValue?.let { onResult(it) }
        }
        .addOnCompleteListener { proxy.close() }
}

@Composable
private fun PermissionDeniedView(onDismiss: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .fillMaxSize()
            .background(TracksyBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.QrCodeScanner,
                contentDescription = null,
                tint               = TracksyPrimary,
                modifier           = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text       = "Permiso de cámara requerido",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text      = "Para escanear códigos de barra necesitás habilitar el acceso a la cámara en la configuración.",
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onDismiss,
                shape   = RoundedCornerShape(50)
            ) {
                Text("Volver")
            }
        }
    }
}
