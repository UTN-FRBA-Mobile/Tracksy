package com.example.tracksy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.tracksy.screens.BarcodeScannerScreen
import com.example.tracksy.screens.HomeScreen
import com.example.tracksy.ui.theme.TracksyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TracksyTheme {
                var showScanner by remember { mutableStateOf(false) }

                if (showScanner) {
                    BarcodeScannerScreen(
                        onBarcodeDetected = { showScanner = false },
                        onDismiss = { showScanner = false }
                    )
                } else {
                    HomeScreen(onScannerOpen = { showScanner = true })
                }
            }
        }
    }
}