package com.example.tracksy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.local.UserPreferencesRepository
import com.example.tracksy.location.LocationService
import com.example.tracksy.location.ProximityTargets
import com.example.tracksy.location.SupermarketTarget
import com.example.tracksy.recommendations.RecommendationWorker
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
import com.example.tracksy.ui.lists.ItemDeLista
import com.example.tracksy.ui.lists.MisListasScreen
import com.example.tracksy.ui.profile.CambiarContrasenaScreen
import com.example.tracksy.ui.profile.EditarPerfilScreen
import com.example.tracksy.ui.profile.PerfilScreen
import com.example.tracksy.ui.profile.PerfilUsuario
import com.example.tracksy.ui.supermarket.CompararSupermercadosScreen
import com.example.tracksy.ui.components.TracksyTextAction
import com.example.tracksy.ui.theme.LocalTracksyColors
import com.example.tracksy.ui.theme.TracksyTheme
import com.example.tracksy.viewmodel.AuthViewModel
import com.example.tracksy.viewmodel.CompraViewModel
import com.example.tracksy.viewmodel.ListaViewModel
import com.example.tracksy.viewmodel.PerfilViewModel
import com.example.tracksy.viewmodel.ProductoViewModel
import com.example.tracksy.viewmodel.RecommendationViewModel

enum class AppScreen {
    EditarLista, DetalleLista, CompararSupermercados, FinalizarCompra
}

class MainActivity : ComponentActivity() {

    private val tokenManager by lazy { TokenManager(this) }
    private val userPrefs    by lazy { UserPreferencesRepository(this) }

    companion object {
        // Activar para testear sin backend ni Firebase
        const val DEBUG_BYPASS_AUTH = true
    }

    private fun loadDebugMockData() {
        val storage = com.example.tracksy.data.local.RecommendationStorage(this)
        if (storage.loadVisible().isEmpty()) {
            storage.mergeAndSave(
                listOf(
                    com.example.tracksy.recommendations.Recommendation(
                        productoId = 7790001001234L,
                        productoNombre = "Leche La Serenísima 1L",
                        criterionType = com.example.tracksy.recommendations.RecommendationCriterionType.FAVORITO_NO_PLANIFICADO,
                        reason = "Es tu favorito y no está planificado"
                    ),
                    com.example.tracksy.recommendations.Recommendation(
                        productoId = 7790001005678L,
                        productoNombre = "Pan Lactal Bimbo",
                        criterionType = com.example.tracksy.recommendations.RecommendationCriterionType.PRODUCTO_FRECUENTE,
                        reason = "Lo compraste 4 veces en el último mes"
                    ),
                    com.example.tracksy.recommendations.Recommendation(
                        productoId = 7790001009999L,
                        productoNombre = "Yogur Activia x4",
                        criterionType = com.example.tracksy.recommendations.RecommendationCriterionType.PRODUCTO_FRECUENTE,
                        reason = "Lo compraste 3 veces en el último mes"
                    )
                )
            )
        }

        // Supermercado mock cercano — reemplazar lat/lon por las del dispositivo de prueba
        com.example.tracksy.location.ProximityTargets.targets = listOf(
            com.example.tracksy.location.SupermarketTarget(
                supermercadoId = 1,
                nombre = "Carrefour Palermo",
                listaNombre = "Lista del super (demo)",
                latitud = -34.5885,   // <-- reemplazar con coordenadas reales para la prueba
                longitud = -58.4310
            )
        )
    }

    private val authViewModel:           AuthViewModel           by viewModels { AuthViewModel.Factory(this) }
    private val perfilViewModel:         PerfilViewModel         by viewModels { PerfilViewModel.Factory(this) }
    private val productoViewModel:       ProductoViewModel       by viewModels { ProductoViewModel.Factory(this) }
    private val listaViewModel:          ListaViewModel          by viewModels { ListaViewModel.Factory(this) }
    private val compraViewModel:         CompraViewModel         by viewModels { CompraViewModel.Factory(this) }
    private val recommendationViewModel: RecommendationViewModel by viewModels { RecommendationViewModel.Factory(this) }

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
                val productoEscaneado      by productoViewModel.productoEscaneado.collectAsState()
                val productoNoEncontrado   by productoViewModel.productoNoEncontrado.collectAsState()
                val listas               by listaViewModel.listas.collectAsState()
                val listasDetalladas     by listaViewModel.listasDetalladas.collectAsState()
                val listaRecienCreada    by listaViewModel.listaRecienCreada.collectAsState()
                val listaActual          by listaViewModel.listaActual.collectAsState()
                val supermercados        by listaViewModel.supermercados.collectAsState()
                val listados             by listaViewModel.listados.collectAsState()
                val compras              by compraViewModel.compras.collectAsState()

                // ── Preferencias de usuario ──────────────────────────────────
                var notificacionesEnabled     by remember { mutableStateOf(userPrefs.notificationsEnabled) }
                var alertasEnabled            by remember { mutableStateOf(userPrefs.proximityAlertsEnabled) }
                var distanciaMetros           by remember { mutableStateOf(userPrefs.proximityDistanceMeters) }

                // ── Permisos ─────────────────────────────────────────────────
                var hasLocationPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED
                    )
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                }

                // ── Sugerencias del motor de recomendaciones ─────────────────
                val recommendations by recommendationViewModel.recommendations.collectAsState()
                val sugerenciasVisibles = recommendations.map { rec ->
                    Suggestion(
                        productoId = rec.productoId,
                        emoji = when (rec.criterionType) {
                            com.example.tracksy.recommendations.RecommendationCriterionType.FAVORITO_NO_PLANIFICADO -> "⭐"
                            com.example.tracksy.recommendations.RecommendationCriterionType.PRODUCTO_FRECUENTE -> "🔄"
                        },
                        name = rec.productoNombre,
                        reason = rec.reason
                    )
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
                var addingProductToList       by remember { mutableStateOf(false) }
                var productoParaLista         by remember { mutableStateOf<Product?>(null) }
                var preloadedListItem         by remember { mutableStateOf<ItemDeLista?>(null) }
                var productPendingReturn      by remember { mutableStateOf<Product?>(null) }
                var draftProductSelections    by remember { mutableStateOf<Map<Int, Int>?>(null) }

                // Cargar datos al autenticarse
                LaunchedEffect(isAuthenticated) {
                    if (isAuthenticated || DEBUG_BYPASS_AUTH) {
                        if (DEBUG_BYPASS_AUTH) {
                            loadDebugMockData()
                        } else {
                            perfilViewModel.cargarPerfil()
                            productoViewModel.cargarProductos()
                            productoViewModel.cargarFavoritos()
                            listaViewModel.cargarListas()
                            listaViewModel.cargarEstadosProducto()
                            listaViewModel.cargarSupermercados()
                            compraViewModel.cargarCompras()
                            recommendationViewModel.refresh()
                            RecommendationWorker.schedule(this@MainActivity)
                        }

                        val permsToRequest = buildList {
                            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                                add(Manifest.permission.ACCESS_FINE_LOCATION)
                                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        if (permsToRequest.isNotEmpty()) {
                            permissionLauncher.launch(permsToRequest.toTypedArray())
                        }
                    }
                }

                // Actualizar objetivos de proximidad cuando cambian las listas activas
                // (en modo debug los targets ya fueron cargados por loadDebugMockData)
                LaunchedEffect(listasDetalladas, supermercados) {
                    if (DEBUG_BYPASS_AUTH) return@LaunchedEffect
                    ProximityTargets.targets = listasDetalladas
                        .mapNotNull { lista ->
                            val super_ = supermercados.find { it.id == lista.supermercado }
                                ?: return@mapNotNull null
                            SupermarketTarget(
                                supermercadoId = super_.id,
                                nombre = super_.nombre,
                                listaNombre = lista.nombre,
                                latitud = super_.latitud,
                                longitud = super_.longitud
                            )
                        }
                        .distinctBy { it.supermercadoId }
                }

                // Iniciar o detener el servicio de ubicación según permisos y preferencias
                LaunchedEffect(isAuthenticated, hasLocationPermission, alertasEnabled) {
                    if (isAuthenticated && hasLocationPermission && alertasEnabled) {
                        startForegroundService(Intent(this@MainActivity, LocationService::class.java))
                    } else {
                        stopService(Intent(this@MainActivity, LocationService::class.java))
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

                // Resuelve el producto escaneado según el contexto:
                // - desde lista → agrega directamente a la lista (productoParaLista)
                // - fuera de lista → abre ProductDetailScreen
                LaunchedEffect(productoEscaneado) {
                    val p = productoEscaneado ?: return@LaunchedEffect
                    if (addingProductToList) {
                        productoParaLista     = p
                        addingProductToList   = false
                    } else {
                        selectedProduct = p
                        listaViewModel.cargarListas()
                    }
                    productoViewModel.limpiarProductoEscaneado()
                }

                // Limpia el flag cuando crearListaConItems termina y listasDetalladas se actualizó.
                // La navegación ya ocurrió; LaunchedEffect(initialSelections) en
                // ProductDetailScreen se encarga de agregar la nueva lista al borrador.
                LaunchedEffect(listaRecienCreada) {
                    if (listaRecienCreada != null) {
                        listaViewModel.limpiarListaRecienCreada()
                    }
                }

                // Cargar detalle de lista cuando se selecciona una
                LaunchedEffect(selectedList) {
                    selectedList?.let {
                        listaViewModel.cargarLista(it.id)
                        listaViewModel.cargarListados()
                    }
                }

                // Cargar listados de precios al entrar a la pantalla de comparación
                LaunchedEffect(currentScreen) {
                    if (currentScreen == AppScreen.CompararSupermercados) {
                        listaViewModel.cargarListados()
                    }
                }

                val colors = LocalTracksyColors.current
                val usuario = perfilState ?: PerfilUsuario("", "")

                Crossfade(
                    targetState = if (DEBUG_BYPASS_AUTH) true else isAuthenticated,
                    animationSpec = tween(400),
                    label = "auth_transition"
                ) { authenticated ->
                    if (!authenticated) {
                        TracksyAuthApp(
                            onAuthenticated = { },
                            onLogin = { email, password -> authViewModel.login(email, password) },
                            onCreateAccount = { nombre, email, password -> authViewModel.registro(nombre, email, password) },
                            onRecoverPassword = { email -> authViewModel.sendPasswordReset(email) },
                            onResendEmailVerification = { authViewModel.resendEmailVerification() },
                            onRefreshEmailVerification = { authViewModel.refreshEmailVerification() },
                            onCancelPendingEmailVerification = { authViewModel.logout() }
                        )
                    } else {
                        val onTabChange: (NavTab) -> Unit = { tab ->
                            selectedProduct           = null
                            selectedHistoryItem       = null
                            selectedList              = null
                            currentScreen             = AppScreen.EditarLista
                            showEditarListaStandalone = false
                            preloadedListItem         = null
                            productPendingReturn      = null
                            draftProductSelections    = null
                            addingProductToList       = false
                            productoParaLista         = null
                            listaViewModel.limpiarListaRecienCreada()
                            if (tab == NavTab.SCANNER) showScanner = true
                            else selectedTab = tab
                        }

                        // ── Pull-to-refresh helpers por tab ───────────────────
                        val isLoadingProductos by productoViewModel.isLoading.collectAsState()
                        val isLoadingListas    by listaViewModel.isLoading.collectAsState()
                        val isLoadingCompras   by compraViewModel.isRefreshing.collectAsState()

                        when {
                            showCambiarContrasena -> CambiarContrasenaScreen(
                                onChangePassword = { passwordActual, passwordNuevo ->
                                    perfilViewModel.cambiarPassword(passwordActual, passwordNuevo)
                                },
                                onBack    = { showCambiarContrasena = false },
                                onSuccess = { showCambiarContrasena = false }
                            )

                            showEditarPerfil -> EditarPerfilScreen(
                                usuario = usuario,
                                onSave = { updated ->
                                    perfilViewModel.actualizarPerfil(updated.nombre, updated.fotoUri)
                                    showEditarPerfil = false
                                },
                                onBack = { showEditarPerfil = false }
                            )

                            showPerfil -> PerfilScreen(
                                usuario    = usuario,
                                isDarkMode = isDarkMode,
                                notificacionesEnabled = notificacionesEnabled,
                                onNotificacionesChange = {
                                    notificacionesEnabled = it
                                    userPrefs.notificationsEnabled = it
                                },
                                alertasSupermercadoEnabled = alertasEnabled,
                                onAlertasSupermercadoChange = {
                                    alertasEnabled = it
                                    userPrefs.proximityAlertsEnabled = it
                                },
                                distanciaMetros = distanciaMetros,
                                onDistanciaChange = {
                                    distanciaMetros = it
                                    userPrefs.proximityDistanceMeters = it
                                },
                                onBack     = { showPerfil = false },
                                onLogout   = {
                                    authViewModel.logout()
                                    recommendationViewModel.clearOnLogout()
                                    showPerfil                = false
                                    selectedTab               = NavTab.HOME
                                    selectedProduct           = null
                                    selectedHistoryItem       = null
                                    selectedList              = null
                                    showEditarListaStandalone = false
                                    showScanner               = false
                                    pendingBarcode            = null
                                    addingProductToList       = false
                                    productoParaLista         = null
                                    preloadedListItem         = null
                                    productPendingReturn      = null
                                    draftProductSelections    = null
                                    listaViewModel.limpiarListaRecienCreada()
                                    stopService(Intent(this@MainActivity, LocationService::class.java))
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
                                        addingProductToList = true
                                        scannerFromList = false
                                    }
                                    productoViewModel.buscarProductoPorBarcode(barcode)
                                    showScanner = false
                                },
                                onDismiss = {
                                    scannerFromList = false
                                    showScanner = false
                                }
                            )

                            selectedHistoryItem != null -> HistoryDetailScreen(
                                item        = selectedHistoryItem!!,
                                profilePhotoUri = usuario.fotoUri,
                                onBackClick = { selectedHistoryItem = null }
                            )

                            selectedProduct != null -> ProductDetailScreen(
                                product          = selectedProduct!!,
                                listas           = listasDetalladas,
                                draftSelections  = draftProductSelections,
                                profilePhotoUri  = usuario.fotoUri,
                                onBack      = {
                                    draftProductSelections = null
                                    selectedProduct = null
                                },
                                selectedTab = selectedTab,
                                onTabChange = onTabChange,
                                onConfirmarCambios = { productoId, agregar, eliminar, actualizar ->
                                    listaViewModel.aplicarCambiosProducto(productoId, agregar, eliminar, actualizar)
                                    draftProductSelections = null
                                    selectedProduct = null
                                },
                                onNuevaLista = { currentSelections ->
                                    draftProductSelections = currentSelections
                                    productPendingReturn = selectedProduct
                                    preloadedListItem = selectedProduct?.let {
                                        ItemDeLista(productoId = it.id, nombre = it.name, cantidad = 1)
                                    }
                                    selectedProduct           = null
                                    showEditarListaStandalone = true
                                }
                            )

                            // ── Nueva lista standalone ────────────────────────
                            showEditarListaStandalone -> EditarListaScreen(
                                listaActual          = null,
                                productosDisponibles = productosBusqueda,
                                supermercados        = supermercados,
                                preloadedItems       = listOfNotNull(preloadedListItem),
                                onConfirmar = { nombre, supermercadoId, items ->
                                    listaViewModel.crearListaConItems(
                                        nombre         = nombre,
                                        supermercadoId = supermercadoId,
                                        items          = items.map { it.productoId to it.cantidad }
                                    )
                                    productoViewModel.limpiarBusquedaLista()
                                    showEditarListaStandalone = false
                                    pendingBarcode            = null
                                    preloadedListItem         = null
                                    val retorno      = productPendingReturn
                                    productPendingReturn = null
                                    if (retorno != null) {
                                        // Volvemos de inmediato a ProductDetailScreen.
                                        // LaunchedEffect(initialSelections) en esa screen
                                        // agrega la nueva lista en cuanto listasDetalladas
                                        // se actualiza (async via crearListaConItems).
                                        selectedProduct = retorno
                                    } else {
                                        draftProductSelections = null
                                        selectedTab = NavTab.LISTS
                                    }
                                },
                                onBack = {
                                    productoViewModel.limpiarBusquedaLista()
                                    showEditarListaStandalone = false
                                    pendingBarcode            = null
                                    preloadedListItem         = null
                                    // Si vinimos desde ProductDetailScreen, volvemos a ese producto
                                    // El borrador se mantiene (no se limpia) para preservar el estado
                                    selectedProduct      = productPendingReturn
                                    productPendingReturn = null
                                },
                                onScanBarcode    = {
                                    scannerFromList = true
                                    showScanner     = true
                                },
                                onBuscarCatalogo = { productoViewModel.buscarProductosParaLista(it) },
                                scannedProductToAdd      = productoParaLista,
                                onScannedProductConsumed = { productoParaLista = null }
                            )

                            // ── Flujo de lista seleccionada ───────────────────
                            selectedList != null -> when (currentScreen) {

                                AppScreen.DetalleLista -> DetalleListaScreen(
                                    lista         = listaActual,
                                    supermercados = supermercados,
                                    listados      = listados,
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
                                    scannedProductToAdd      = productoParaLista,
                                onScannedProductConsumed = { productoParaLista = null }
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
                                    val esComprado: (String) -> Boolean = { estadoNombre ->
                                        estadoNombre.lowercase().let { n -> n.contains("comprad") || n.contains("completad") }
                                    }
                                    val comprados  = realItems.count { esComprado(it.estadoNombre) }
                                    val pendientes = realItems.filter { !esComprado(it.estadoNombre) }
                                    val superId    = listaActual?.supermercado
                                    val preciosDelSuper = if (superId != null) {
                                        listados.filter { it.supermercado == superId }.associate { it.producto to it.precio }
                                    } else emptyMap<Long, Double>()
                                    val totalGastado = realItems
                                        .filter { esComprado(it.estadoNombre) }
                                        .sumOf { item -> (preciosDelSuper[item.producto] ?: item.precioUnitario) * item.cantidad }

                                    FinalizarCompraScreen(
                                        summary = PurchaseSummary(
                                            listName          = lista.name,
                                            purchasedProducts = comprados,
                                            totalProducts     = realItems.size,
                                            totalSpent        = totalGastado.toInt(),
                                            pendingItems      = pendientes.map { it.productoNombre }
                                        ),
                                        onBack = { currentScreen = AppScreen.DetalleLista },
                                        onCrearListaPendientes = { nombre ->
                                            listaViewModel.crearListaConItems(
                                                nombre         = nombre,
                                                supermercadoId = null,
                                                items          = pendientes.map { it.producto to it.cantidad }
                                            )
                                        },
                                        onConfirm = {
                                            compraViewModel.crearCompra(
                                                supermercadoId = superId,
                                                nombreLista    = lista.name,
                                                total          = totalGastado,
                                                productos      = realItems
                                                    .filter { esComprado(it.estadoNombre) }
                                                    .map { item -> Triple(item.producto, item.cantidad, preciosDelSuper[item.producto] ?: item.precioUnitario) }
                                            )
                                            listaViewModel.eliminarLista(lista.id)
                                            selectedList  = null
                                            currentScreen = AppScreen.DetalleLista
                                            selectedTab   = NavTab.HISTORY
                                        }
                                    )
                                }
                            }

                            selectedTab == NavTab.LISTS -> MisListasScreen(
                                selectedTab     = selectedTab,
                                onTabChange     = onTabChange,
                                listas          = listas,
                                profilePhotoUri = usuario.fotoUri,
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
                                profilePhotoUri = usuario.fotoUri,
                                onProfileClick = { showPerfil = true },
                                isRefreshing   = isLoadingCompras,
                                onRefresh      = { compraViewModel.cargarCompras() }
                            )

                            selectedTab == NavTab.PRODUCTS -> ProductsScreen(
                                selectedTab      = selectedTab,
                                onTabChange      = onTabChange,
                                productosApi     = productos,
                                favoritosApi     = favoritos,
                                profilePhotoUri  = usuario.fotoUri,
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
                                userName       = usuario.nombre,
                                profilePhotoUri = usuario.fotoUri,
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
                                onAgregarSugerencia = { suggestion, listaId ->
                                    if (suggestion.productoId != null) {
                                        val estadoId = listaViewModel.idEstadoPendiente()
                                            ?: listaViewModel.estadosProducto.value.firstOrNull()?.id
                                        estadoId?.let {
                                            listaViewModel.agregarItem(
                                                listaId    = listaId,
                                                productoId = suggestion.productoId,
                                                cantidad   = 1,
                                                estadoId   = it,
                                                precio     = 0.0
                                            )
                                        }
                                        recommendationViewModel.dismiss(suggestion.productoId)
                                    }
                                },
                                onDismissSugerencia = { suggestion ->
                                    suggestion.productoId?.let { recommendationViewModel.dismiss(it) }
                                }
                            )
                        }

                        if (productoNoEncontrado) {
                            AlertDialog(
                                onDismissRequest = { productoViewModel.limpiarProductoNoEncontrado() },
                                title = { Text("Producto no encontrado") },
                                text  = { Text("El producto escaneado no existe o no fue encontrado.") },
                                confirmButton = {
                                    TracksyTextAction(
                                        text = "Reintentar",
                                        onClick = {
                                            productoViewModel.limpiarProductoNoEncontrado()
                                            if (addingProductToList) scannerFromList = true
                                            showScanner = true
                                        }
                                    )
                                },
                                dismissButton = {
                                    TracksyTextAction(
                                        text = "Cancelar",
                                        onClick = { productoViewModel.limpiarProductoNoEncontrado() },
                                        contentColor = colors.subtitleText
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
