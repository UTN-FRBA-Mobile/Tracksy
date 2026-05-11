package com.example.tracksy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.tracksy.screens.BarcodeScannerScreen
import com.example.tracksy.screens.HomeScreen
import com.example.tracksy.screens.NavTab
import com.example.tracksy.screens.Product
import com.example.tracksy.screens.ProductDetailScreen
import com.example.tracksy.screens.ProductsScreen
import com.example.tracksy.ui.theme.TracksyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TracksyTheme {
                var selectedTab     by remember { mutableStateOf(NavTab.HOME) }
                var showScanner     by remember { mutableStateOf(false) }
                var selectedProduct by remember { mutableStateOf<Product?>(null) }

                val onTabChange: (NavTab) -> Unit = { tab ->
                    selectedProduct = null
                    if (tab == NavTab.SCANNER) showScanner = true
                    else selectedTab = tab
                }

                when {
                    showScanner -> BarcodeScannerScreen(
                        onBarcodeDetected = { showScanner = false },
                        onDismiss         = { showScanner = false }
                    )
                    selectedProduct != null -> ProductDetailScreen(
                        product     = selectedProduct!!,
                        onBack      = { selectedProduct = null },
                        selectedTab = selectedTab,
                        onTabChange = onTabChange
                    )
                    selectedTab == NavTab.PRODUCTS -> ProductsScreen(
                        selectedTab  = selectedTab,
                        onTabChange  = onTabChange,
                        onProductTap = { product -> selectedProduct = product }
                    )
                    else -> HomeScreen(
                        selectedTab = selectedTab,
                        onTabChange = onTabChange
                    )
                }
            }
        }
    }
}