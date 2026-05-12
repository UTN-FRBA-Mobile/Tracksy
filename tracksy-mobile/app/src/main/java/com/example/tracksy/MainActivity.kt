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
import com.example.tracksy.screens.ShoppingList
import com.example.tracksy.ui.auth.TracksyAuthApp
import com.example.tracksy.ui.history.HistoryDetailScreen
import com.example.tracksy.ui.history.HistoryItem
import com.example.tracksy.ui.history.HistoryScreen
import com.example.tracksy.ui.lists.DetalleListaScreen
import com.example.tracksy.ui.lists.EditarListaScreen
import com.example.tracksy.ui.supermarket.CompararSupermercadosScreen
import com.example.tracksy.ui.theme.TracksyTheme

enum class AppScreen {
    EditarLista, DetalleLista, CompararSupermercados
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TracksyTheme {
                var isAuthenticated      by remember { mutableStateOf(false) }
                var selectedTab         by remember { mutableStateOf(NavTab.HOME) }
                var showScanner         by remember { mutableStateOf(false) }
                var selectedProduct     by remember { mutableStateOf<Product?>(null) }
                var selectedHistoryItem by remember { mutableStateOf<HistoryItem?>(null) }
                var selectedList        by remember { mutableStateOf<ShoppingList?>(null) }
                var currentScreen       by remember { mutableStateOf(AppScreen.EditarLista) }

                if (!isAuthenticated) {
                    TracksyAuthApp(onAuthenticated = { isAuthenticated = true })
                } else {
                    val onTabChange: (NavTab) -> Unit = { tab ->
                        selectedProduct     = null
                        selectedHistoryItem = null
                        selectedList        = null
                        currentScreen       = AppScreen.EditarLista
                        if (tab == NavTab.SCANNER) showScanner = true
                        else selectedTab = tab
                    }

                    when {
                        showScanner -> BarcodeScannerScreen(
                            onBarcodeDetected = { showScanner = false },
                            onDismiss         = { showScanner = false }
                        )
                        selectedHistoryItem != null -> HistoryDetailScreen(
                            item        = selectedHistoryItem!!,
                            onBackClick = { selectedHistoryItem = null }
                        )
                        selectedProduct != null -> ProductDetailScreen(
                            product     = selectedProduct!!,
                            onBack      = { selectedProduct = null },
                            selectedTab = selectedTab,
                            onTabChange = onTabChange
                        )
                        selectedList != null -> when (currentScreen) {
                            AppScreen.DetalleLista -> DetalleListaScreen(
                                onEditar = { currentScreen = AppScreen.EditarLista },
                                onComparar = { currentScreen = AppScreen.CompararSupermercados },
                                onBack = {
                                    selectedList = null; currentScreen = AppScreen.DetalleLista
                                }
                            )
                            AppScreen.EditarLista -> EditarListaScreen(
                                onConfirmar = { currentScreen = AppScreen.DetalleLista },
                                onBack = { currentScreen = AppScreen.DetalleLista }
                            )
                            AppScreen.CompararSupermercados -> CompararSupermercadosScreen(
                                onBack = { currentScreen = AppScreen.DetalleLista }
                            )
                        }
                        selectedTab == NavTab.HISTORY -> HistoryScreen(
                            onBackClick = { selectedTab = NavTab.HOME },
                            onItemClick = { item -> selectedHistoryItem = item },
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
                            onTabChange = onTabChange,
                            onListTap   = { list ->
                                selectedList  = list
                                currentScreen = AppScreen.DetalleLista
                            }
                        )
                    }
                }
            }
        }
    }
}
