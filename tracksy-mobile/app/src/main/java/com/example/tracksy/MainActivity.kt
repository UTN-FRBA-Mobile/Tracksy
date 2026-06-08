package com.example.tracksy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.screens.BarcodeScannerScreen
import com.example.tracksy.screens.HomeScreen
import com.example.tracksy.screens.NavTab
import com.example.tracksy.screens.Product
import com.example.tracksy.screens.ProductDetailScreen
import com.example.tracksy.screens.ProductsScreen
import com.example.tracksy.screens.ShoppingList
import com.example.tracksy.screens.Suggestion
import com.example.tracksy.ui.auth.TracksyAuthApp
import com.example.tracksy.ui.checkout.FinalizarCompraScreen
import com.example.tracksy.ui.checkout.PurchaseSummary
import com.example.tracksy.ui.history.HistoryDetailScreen
import com.example.tracksy.ui.history.HistoryItem
import com.example.tracksy.ui.history.HistoryScreen
import com.example.tracksy.ui.lists.DetalleListaScreen
import com.example.tracksy.ui.lists.EditarListaScreen
import com.example.tracksy.ui.lists.MisListasScreen
import com.example.tracksy.ui.profile.CambiarContrasenaScreen
import com.example.tracksy.ui.profile.EditarPerfilScreen
import com.example.tracksy.ui.profile.PerfilScreen
import com.example.tracksy.ui.profile.PerfilUsuario
import com.example.tracksy.ui.supermarket.CompararSupermercadosScreen
import com.example.tracksy.ui.theme.TracksyTheme
import com.example.tracksy.viewmodel.AuthViewModel
import com.example.tracksy.viewmodel.CompraViewModel
import com.example.tracksy.viewmodel.ListaViewModel
import com.example.tracksy.viewmodel.PerfilViewModel
import com.example.tracksy.viewmodel.ProductoViewModel

enum class AppScreen {
    EditarLista, DetalleLista, CompararSupermercados, FinalizarCompra
}

class MainActivity : ComponentActivity() {

    private val tokenManager by lazy { TokenManager(this) }

    private val authViewModel:     AuthViewModel     by viewModels { AuthViewModel.Factory(this) }
    private val perfilViewModel:   PerfilViewModel   by viewModels { PerfilViewModel.Factory(this) }
    private val productoViewModel: ProductoViewModel by viewModels { ProductoViewModel.Factory(this) }
    private val listaViewModel:    ListaViewModel    by viewModels { ListaViewModel.Factory(this) }
    private val compraViewModel:   CompraViewModel   by viewModels { CompraViewModel.Factory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(tokenManager.isDarkMode) }

            TracksyTheme(darkTheme = isDarkMode) {

                // ── ViewModel state ──────────────────────────────────────────
                val isAuthenticated      by authViewModel.isAuthenticated.collectAsState()
                val perfilState          by perfilViewModel.perfil.collectAsState()
                val productos            by productoViewModel.productos.collectAsState()
                val favoritos            by productoViewModel.favoritos.collectAsState()
                val productosBusqueda    by productoViewModel.productosBusqueda.collectAsState()
                val listas               by listaViewModel.listas.collectAsState()
                val listaActual          by listaViewModel.listaActual.collectAsState()
                val supermercados        by listaViewModel.supermercados.collectAsState()
                val listados             by listaViewModel.listados.collectAsState()
                val compras              by compraViewModel.compras.collectAsState()

                // ── Sugerencias derivadas del historial de compras ───────────
                val sugerenciasGeneradas by remember(compras, productos) {
                    derivedStateOf {
                        compras
                            .flatMap { it.products }
                            .groupBy { it.name }
                            .entries
                            .sortedByDescending { it.value.size }
                            .take(5)
                            .map { entry ->
                                val nombre = entry.key
                                val veces = entry.value.size
                                val productoId = productos.firstOrNull { p -> p.name == nombre }?.id
                                Suggestion(
                                    productoId = productoId,
                                    emoji = "🛒",
                                    name = nombre,
                                    reason = "Comprado $veces ${if (veces == 1) "vez" else "veces"}"
                                )
                            }
                    }
                }
                var dismissedSuggestionIds by remember { mutableStateOf(emptySet<Long>()) }
                val sugerenciasVisibles = remember(sugerenciasGeneradas, dismissedSuggestionIds) {
                    sugerenciasGeneradas.filter { (it.productoId ?: -1L) !in dismissedSuggestionIds }
                }

                // ── UI navigation state ──────────────────────────────────────
                var selectedTab               by remember { mutableStateOf(NavTab.HOME) }
                var showScanner               by remember { mutableStateOf(false) }
                var selectedProduct           by remember { mutableStateOf<Product?>(null) }
                var selectedHistoryItem       by remember { mutableStateOf<HistoryItem?>(null) }
                var selectedList              by remember { mutableStateOf<ShoppingList?>(null) }
                var currentScreen             by remember { mutableStateOf(AppScreen.EditarLista) }
                var showEditarListaStandalone by remember { mutableStateOf(false) }
                var showPerfil                by remember { mutableStateOf(false) }
                var showEditarPerfil          by remember { mutableStateOf(false) }
                var showCambiarContrasena     by remember { mutableStateOf(false) }
                var scannerFromList           by remember { mutableStateOf(false) }
                var pendingBarcode            by remember { mutableStateOf<String?>(null) }

                // Cargar datos al autenticarse
                LaunchedEffect(isAuthenticated) {
                    if (isAuthenticated) {
                        perfilViewModel.cargarPerfil()
                        productoViewModel.cargarProductos()
                        productoViewModel.cargarFavoritos()
                        listaViewModel.cargarListas()
                        listaViewModel.cargarEstadosProducto()
                        listaViewModel.cargarSupermercados()
                        compraViewModel.cargarCompras()
                    }
                }

                // Auto-refresh al cambiar de tab (sincroniza los datos de la pantalla activa)
                LaunchedEffect(selectedTab) {
                    if (isAuthenticated) {
                        when (selectedTab) {
                            NavTab.HOME -> {
                                listaViewModel.cargarListas()
                                compraViewModel.cargarCompras()
                            }
                            NavTab.LISTS -> listaViewModel.cargarListas()
                            NavTab.PRODUCTS -> {
                                productoViewModel.cargarProductos()
                                productoViewModel.cargarFavoritos()
                            }
                            NavTab.HISTORY -> compraViewModel.cargarCompras()
                            NavTab.SCANNER -> Unit
                        }
                    }
                }

                // Cargar detalle de lista cuando se selecciona una
                LaunchedEffect(selectedList) {
                    selectedList?.let { listaViewModel.cargarLista(it.id) }
                }

                // Cargar listados de precios al entrar a la pantalla de comparación
                LaunchedEffect(currentScreen) {
                    if (currentScreen == AppScreen.CompararSupermercados) {
                        listaViewModel.cargarListados()
                    }
                }

                val usuario = perfilState ?: PerfilUsuario("", "")

                Crossfade(
                    targetState = isAuthenticated,
                    animationSpec = tween(400),
                    label = "auth_transition"
                ) { authenticated ->
                    if (!authenticated) {
                        TracksyAuthApp(
                            onAuthenticated = { },
                            onLogin = { email, password -> authViewModel.login(email, password) },
                            onCreateAccount = { nombre, email, password -> authViewModel.registro(nombre, email, password) }
                        )
                    } else {
                        val onTabChange: (NavTab) -> Unit = { tab ->
                            selectedProduct           = null
                            selectedHistoryItem       = null
                            selectedList              = null
                            currentScreen             = AppScreen.EditarLista
                            showEditarListaStandalone = false
                            if (tab == NavTab.SCANNER) showScanner = true
                            else selectedTab = tab
                        }

                        // ── Pull-to-refresh helpers por tab ───────────────────
                        val isLoadingProductos by productoViewModel.isLoading.collectAsState()
                        val isLoadingListas    by listaViewModel.isLoading.collectAsState()
                        val isLoadingCompras   by compraViewModel.isRefreshing.collectAsState()

                        when {
                            showCambiarContrasena -> CambiarContrasenaScreen(
                                emailUsuario = usuario.email,
                                onBack    = { showCambiarContrasena = false },
                                onSuccess = { showCambiarContrasena = false }
                            )

                            showEditarPerfil -> EditarPerfilScreen(
                                usuario = usuario,
                                onSave = { updated ->
                                    perfilViewModel.actualizarPerfil(updated.nombre)
                                    showEditarPerfil = false
                                },
                                onBack = { showEditarPerfil = false }
                            )

                            showPerfil -> PerfilScreen(
                                usuario    = usuario,
                                isDarkMode = isDarkMode,
                                onBack     = { showPerfil = false },
                                onLogout   = {
                                    authViewModel.logout()
                                    showPerfil                = false
                                    selectedTab               = NavTab.HOME
                                    selectedProduct           = null
                                    selectedHistoryItem       = null
                                    selectedList              = null
                                    showEditarListaStandalone = false
                                    showScanner               = false
                                    pendingBarcode            = null
                                    dismissedSuggestionIds    = emptySet()
                                },
                                onEditarPerfil      = { showEditarPerfil = true },
                                onCambiarContrasena = { showCambiarContrasena = true },
                                onModoOscuroChange  = {
                                    isDarkMode = it
                                    tokenManager.isDarkMode = it
                                }
                            )

                            showScanner -> BarcodeScannerScreen(
                                onBarcodeDetected = { barcode ->
                                    if (scannerFromList) {
                                        pendingBarcode = barcode
                                        scannerFromList = false
                                    } else {
                                        productoViewModel.cargarProductos(busqueda = barcode)
                                        selectedTab = NavTab.PRODUCTS
                                    }
                                    showScanner = false
                                },
                                onDismiss = {
                                    scannerFromList = false
                                    showScanner = false
                                }
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

                            // ── Nueva lista standalone ────────────────────────
                            showEditarListaStandalone -> EditarListaScreen(
                                listaActual          = null,
                                productosDisponibles = productosBusqueda,
                                supermercados        = supermercados,
                                onConfirmar = { nombre, supermercadoId, items ->
                                    listaViewModel.crearListaConItems(
                                        nombre         = nombre,
                                        supermercadoId = supermercadoId,
                                        items          = items.map { it.productoId to it.cantidad }
                                    )
                                    productoViewModel.limpiarBusquedaLista()
                                    showEditarListaStandalone = false
                                    pendingBarcode            = null
                                    selectedTab               = NavTab.LISTS
                                },
                                onBack = {
                                    productoViewModel.limpiarBusquedaLista()
                                    showEditarListaStandalone = false
                                    pendingBarcode            = null
                                },
                                onScanBarcode    = {
                                    scannerFromList = true
                                    showScanner     = true
                                },
                                onBuscarCatalogo = { productoViewModel.buscarProductosParaLista(it) },
                                scannedBarcode   = pendingBarcode
                            )

                            // ── Flujo de lista seleccionada ───────────────────
                            selectedList != null -> when (currentScreen) {

                                AppScreen.DetalleLista -> DetalleListaScreen(
                                    lista         = listaActual,
                                    supermercados = supermercados,
                                    onToggleItem  = { listaId, itemId, estaComprado ->
                                        listaViewModel.toggleItem(listaId, itemId, estaComprado)
                                    },
                                    onEditar    = { currentScreen = AppScreen.EditarLista },
                                    onComparar  = { currentScreen = AppScreen.CompararSupermercados },
                                    onFinalizar = { currentScreen = AppScreen.FinalizarCompra },
                                    onBack      = {
                                        selectedList  = null
                                        currentScreen = AppScreen.DetalleLista
                                    }
                                )

                                AppScreen.EditarLista -> EditarListaScreen(
                                    listaActual          = listaActual,
                                    productosDisponibles = productosBusqueda,
                                    supermercados        = supermercados,
                                    onConfirmar = { nombre, supermercadoId, items ->
                                        val listaId = listaActual?.id
                                        if (listaId != null) {
                                            // editarListaConItems hace el diff completo:
                                            // agrega items nuevos, elimina los quitados,
                                            // y recarga _listaActual al finalizar.
                                            listaViewModel.editarListaConItems(
                                                listaId        = listaId,
                                                nombre         = nombre,
                                                supermercadoId = supermercadoId,
                                                items          = items.map { it.productoId to it.cantidad }
                                            )
                                        }
                                        productoViewModel.limpiarBusquedaLista()
                                        currentScreen = AppScreen.DetalleLista
                                    },
                                    onBack = {
                                        productoViewModel.limpiarBusquedaLista()
                                        currentScreen = AppScreen.DetalleLista
                                    },
                                    onScanBarcode    = {
                                        scannerFromList = true
                                        showScanner     = true
                                    },
                                    onBuscarCatalogo = { productoViewModel.buscarProductosParaLista(it) },
                                    scannedBarcode   = pendingBarcode
                                )

                                AppScreen.CompararSupermercados -> CompararSupermercadosScreen(
                                    lista         = listaActual,
                                    supermercados = supermercados,
                                    listados      = listados,
                                    onBack        = { currentScreen = AppScreen.DetalleLista }
                                )

                                AppScreen.FinalizarCompra -> {
                                    val lista     = selectedList!!
                                    val realItems = listaActual?.items ?: emptyList()
                                    val comprados = realItems.count {
                                        it.estadoNombre.lowercase().let { n -> n.contains("comprad") || n.contains("completad") }
                                    }
                                    val pendientes = realItems.filter {
                                        !it.estadoNombre.lowercase().let { n -> n.contains("comprad") || n.contains("completad") }
                                    }
                                    val totalGastado = realItems
                                        .filter { it.estadoNombre.lowercase().let { n -> n.contains("comprad") || n.contains("completad") } }
                                        .sumOf { it.precioUnitario * it.cantidad }

                                    FinalizarCompraScreen(
                                        summary = PurchaseSummary(
                                            listName          = lista.name,
                                            purchasedProducts = comprados,
                                            totalProducts     = realItems.size,
                                            totalSpent        = totalGastado.toInt(),
                                            pendingItems      = pendientes.map { it.productoNombre }
                                        ),
                                        onBack = { currentScreen = AppScreen.DetalleLista },
                                        onConfirm = { createPendingList ->
                                            val supermercadoId = listaActual?.supermercado
                                            if (supermercadoId != null && comprados > 0) {
                                                compraViewModel.crearCompra(
                                                    supermercadoId = supermercadoId,
                                                    total          = totalGastado,
                                                    productos      = realItems
                                                        .filter { it.estadoNombre.lowercase().let { n -> n.contains("comprad") || n.contains("completad") } }
                                                        .map { Triple(it.producto, it.cantidad, it.precioUnitario) }
                                                )
                                            }
                                            selectedList  = null
                                            currentScreen = AppScreen.DetalleLista
                                            compraViewModel.cargarCompras()
                                            if (createPendingList) {
                                                showEditarListaStandalone = true
                                            } else {
                                                selectedTab = NavTab.HOME
                                            }
                                        }
                                    )
                                }
                            }

                            selectedTab == NavTab.LISTS -> MisListasScreen(
                                selectedTab     = selectedTab,
                                onTabChange     = onTabChange,
                                listas          = listas,
                                onListClick     = { list ->
                                    selectedList  = list
                                    currentScreen = AppScreen.DetalleLista
                                },
                                onCreateNewList = { showEditarListaStandalone = true },
                                onDeleteList    = { list -> listaViewModel.eliminarLista(list.id) },
                                onProfileClick  = { showPerfil = true },
                                isRefreshing    = isLoadingListas,
                                onRefresh       = { listaViewModel.cargarListas() }
                            )

                            selectedTab == NavTab.HISTORY -> HistoryScreen(
                                onBackClick    = { selectedTab = NavTab.HOME },
                                onItemClick    = { item -> selectedHistoryItem = item },
                                items          = compras,
                                selectedTab    = selectedTab,
                                onTabChange    = onTabChange,
                                onProfileClick = { showPerfil = true },
                                isRefreshing   = isLoadingCompras,
                                onRefresh      = { compraViewModel.cargarCompras() }
                            )

                            selectedTab == NavTab.PRODUCTS -> ProductsScreen(
                                selectedTab      = selectedTab,
                                onTabChange      = onTabChange,
                                productosApi     = productos,
                                favoritosApi     = favoritos,
                                onProductTap     = { product -> selectedProduct = product },
                                onProfileClick   = { showPerfil = true },
                                onSearchChange   = { query ->
                                    productoViewModel.cargarProductos(query.takeIf { it.isNotBlank() })
                                },
                                onToggleFavorito = { id, esFavorito ->
                                    productoViewModel.toggleFavorito(id, esFavorito)
                                },
                                isRefreshing     = isLoadingProductos,
                                onRefresh        = {
                                    productoViewModel.cargarProductos()
                                    productoViewModel.cargarFavoritos()
                                }
                            )

                            else -> HomeScreen(
                                selectedTab    = selectedTab,
                                onTabChange    = onTabChange,
                                listas         = listas,
                                sugerencias    = sugerenciasVisibles,
                                isRefreshing   = isLoadingListas,
                                onRefresh      = {
                                    listaViewModel.cargarListas()
                                    compraViewModel.cargarCompras()
                                },
                                onListTap      = { list ->
                                    selectedList  = list
                                    currentScreen = AppScreen.DetalleLista
                                },
                                onProfileClick = { showPerfil = true },
                                onAgregarSugerencia = { suggestion ->
                                    val ultimaLista = listas.firstOrNull()
                                    if (ultimaLista != null && suggestion.productoId != null) {
                                        val estadoId = listaViewModel.idEstadoPendiente()
                                            ?: listaViewModel.estadosProducto.value.firstOrNull()?.id
                                        estadoId?.let {
                                            listaViewModel.agregarItem(
                                                listaId    = ultimaLista.id,
                                                productoId = suggestion.productoId,
                                                cantidad   = 1,
                                                estadoId   = it,
                                                precio     = 0.0
                                            )
                                        }
                                    }
                                    suggestion.productoId?.let {
                                        dismissedSuggestionIds = dismissedSuggestionIds + it
                                    }
                                },
                                onDismissSugerencia = { suggestion ->
                                    suggestion.productoId?.let {
                                        dismissedSuggestionIds = dismissedSuggestionIds + it
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
