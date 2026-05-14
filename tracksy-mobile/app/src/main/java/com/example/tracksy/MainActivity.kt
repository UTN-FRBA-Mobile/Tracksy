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
import com.example.tracksy.ui.checkout.FinalizarCompraScreen
import com.example.tracksy.ui.checkout.PurchaseSummary
import com.example.tracksy.ui.history.HistoryDetailScreen
import com.example.tracksy.ui.history.HistoryItem
import com.example.tracksy.ui.history.HistoryScreen
import com.example.tracksy.ui.lists.DetalleListaScreen
import com.example.tracksy.ui.lists.EditarListaScreen
import com.example.tracksy.ui.lists.MisListasScreen
import com.example.tracksy.ui.profile.PerfilScreen
import com.example.tracksy.ui.profile.PerfilUsuario
import com.example.tracksy.ui.supermarket.CompararSupermercadosScreen
import com.example.tracksy.ui.theme.TracksyTheme

enum class AppScreen {
    EditarLista, DetalleLista, CompararSupermercados, FinalizarCompra
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TracksyTheme {
                var isAuthenticated      by remember { mutableStateOf(false) }
                var selectedTab          by remember { mutableStateOf(NavTab.HOME) }
                var showScanner          by remember { mutableStateOf(false) }
                var selectedProduct      by remember { mutableStateOf<Product?>(null) }
                var selectedHistoryItem  by remember { mutableStateOf<HistoryItem?>(null) }
                var selectedList         by remember { mutableStateOf<ShoppingList?>(null) }
                var currentScreen        by remember { mutableStateOf(AppScreen.EditarLista) }
                var showEditarListaStandalone by remember { mutableStateOf(false) }
                var showPerfil           by remember { mutableStateOf(false) }

                if (!isAuthenticated) {
                    TracksyAuthApp(onAuthenticated = { isAuthenticated = true })
                } else {
                    val onTabChange: (NavTab) -> Unit = { tab ->
                        selectedProduct     = null
                        selectedHistoryItem = null
                        selectedList        = null
                        currentScreen       = AppScreen.EditarLista
                        showEditarListaStandalone = false
                        if (tab == NavTab.SCANNER) showScanner = true
                        else selectedTab = tab
                    }

                    when {
                        showPerfil -> PerfilScreen(
                            usuario  = PerfilUsuario(
                                nombre = "Juan Pérez",
                                email  = "juan.perez@gmail.com"
                            ),
                            onBack   = { showPerfil = false },
                            onLogout = {
                                showPerfil = false
                                isAuthenticated = false
                                selectedTab = NavTab.HOME
                                selectedProduct = null
                                selectedHistoryItem = null
                                selectedList = null
                                showEditarListaStandalone = false
                                showScanner = false
                            }
                        )
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
                        showEditarListaStandalone -> EditarListaScreen(
                            onConfirmar = {
                                showEditarListaStandalone = false
                                selectedTab = NavTab.LISTS
                            },
                            onBack = {
                                showEditarListaStandalone = false
                            }
                        )
                        selectedList != null -> when (currentScreen) {
                            AppScreen.DetalleLista -> DetalleListaScreen(
                                onEditar = { currentScreen = AppScreen.EditarLista },
                                onComparar = { currentScreen = AppScreen.CompararSupermercados },
                                onFinalizar = { currentScreen = AppScreen.FinalizarCompra },
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
                            AppScreen.FinalizarCompra -> FinalizarCompraScreen(
                                summary = PurchaseSummary(
                                    listName          = selectedList!!.name,
                                    purchasedProducts = 5,
                                    totalProducts     = 8,
                                    totalSpent        = 6700,
                                    pendingItems      = listOf("Leche entera", "Pan lactal", "Yogur natural")
                                ),
                                onBack = { currentScreen = AppScreen.DetalleLista },
                                onConfirm = { createPendingList ->
                                    selectedList = null
                                    currentScreen = AppScreen.DetalleLista
                                    if (createPendingList) {
                                        showEditarListaStandalone = true
                                    } else {
                                        selectedTab = NavTab.HOME
                                    }
                                }
                            )
                        }
                        selectedTab == NavTab.LISTS -> MisListasScreen(
                            selectedTab = selectedTab,
                            onTabChange = onTabChange,
                            onListClick = { list ->
                                selectedList  = list
                                currentScreen = AppScreen.DetalleLista
                            },
                            onCreateNewList = { showEditarListaStandalone = true },
                            onProfileClick  = { showPerfil = true }
                        )
                        selectedTab == NavTab.HISTORY -> HistoryScreen(
                            onBackClick    = { selectedTab = NavTab.HOME },
                            onItemClick    = { item -> selectedHistoryItem = item },
                            selectedTab    = selectedTab,
                            onTabChange    = onTabChange,
                            onProfileClick = { showPerfil = true }
                        )
                        selectedTab == NavTab.PRODUCTS -> ProductsScreen(
                            selectedTab    = selectedTab,
                            onTabChange    = onTabChange,
                            onProductTap   = { product -> selectedProduct = product },
                            onProfileClick = { showPerfil = true }
                        )
                        else -> HomeScreen(
                            selectedTab    = selectedTab,
                            onTabChange    = onTabChange,
                            onListTap      = { list ->
                                selectedList  = list
                                currentScreen = AppScreen.DetalleLista
                            },
                            onProfileClick = { showPerfil = true }
                        )
                    }
                }
            }
        }
    }
}
