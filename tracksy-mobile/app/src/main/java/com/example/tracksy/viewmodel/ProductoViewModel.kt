package com.example.tracksy.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.repository.TracksyRepository
import com.example.tracksy.screens.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductoViewModel(
    private val repo: TracksyRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _productos = MutableStateFlow<List<Product>>(emptyList())
    val productos: StateFlow<List<Product>> = _productos

    private val _favoritos = MutableStateFlow<List<Product>>(emptyList())
    val favoritos: StateFlow<List<Product>> = _favoritos

    // Resultados de búsqueda para el selector de productos en listas (estado separado
    // para no pisar la lista general de la pestaña Productos)
    private val _productosBusqueda = MutableStateFlow<List<Product>>(emptyList())
    val productosBusqueda: StateFlow<List<Product>> = _productosBusqueda

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _productoEscaneado = MutableStateFlow<Product?>(null)
    val productoEscaneado: StateFlow<Product?> = _productoEscaneado

    private val _productoNoEncontrado = MutableStateFlow(false)
    val productoNoEncontrado: StateFlow<Boolean> = _productoNoEncontrado

    fun limpiarProductoNoEncontrado() { _productoNoEncontrado.value = false }

    private val token get() = tokenManager.accessToken ?: ""

    fun cargarProductos(busqueda: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pageSize = if (busqueda.isNullOrBlank()) 100 else null
                val response = repo.getProductos(token, search = busqueda, pageSize = pageSize)
                if (response.isSuccessful) {
                    val lista = response.body()?.results?.map { it.toUiProduct() } ?: emptyList()
                    Log.d("ProductoVM", "cargarProductos: ${lista.size} productos (count=${response.body()?.count})")
                    _productos.value = lista
                } else {
                    Log.e("ProductoVM", "cargarProductos error HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ProductoVM", "cargarProductos excepción: ${e.message}", e)
            }
            _isLoading.value = false
        }
    }

    fun cargarFavoritos() {
        viewModelScope.launch {
            try {
                val favResponse = repo.getFavoritos(token)
                if (favResponse.isSuccessful) {
                    val favoritosItems = favResponse.body()?.results ?: return@launch
                    if (favoritosItems.isEmpty()) {
                        _favoritos.value = emptyList()
                        return@launch
                    }
                    // Busca primero en _productos (ya cargados) para evitar HTTP extra.
                    // Si no está en caché local, cae al getProducto individual.
                    val productosEnMemoria = _productos.value
                    val productos = favoritosItems.mapNotNull { fav ->
                        productosEnMemoria.firstOrNull { it.id == fav.producto }
                            ?: try {
                                val r = repo.getProducto(token, fav.producto)
                                if (r.isSuccessful) r.body()?.toUiProduct() else null
                            } catch (_: Exception) { null }
                    }
                    _favoritos.value = productos
                }
            } catch (_: Exception) { }
        }
    }

    fun toggleFavorito(productoId: Long, esFavorito: Boolean) {
        viewModelScope.launch {
            // ── Optimistic update ────────────────────────────────────────────────
            // Actualizar _favoritos ANTES de la llamada a la red para dar feedback
            // visual inmediato (la estrella cambia de estado sin esperar la respuesta).
            val snapshotAntes = _favoritos.value
            if (esFavorito) {
                // Buscar el producto en _productos (visible en pantalla) o en favoritos actuales
                val producto = _productos.value.firstOrNull { it.id == productoId }
                    ?: _favoritos.value.firstOrNull { it.id == productoId }
                if (producto != null && productoId !in _favoritos.value.map { it.id }) {
                    _favoritos.value = _favoritos.value + producto
                }
            } else {
                _favoritos.value = _favoritos.value.filter { it.id != productoId }
            }

            // ── Persistir en la API ──────────────────────────────────────────────
            try {
                if (esFavorito) {
                    repo.addFavorito(token, productoId)
                } else {
                    // Necesitamos el ID del registro ProductoUsuario para DELETE /{id}/
                    val favResponse = repo.getFavoritos(token)
                    if (favResponse.isSuccessful) {
                        val favId = favResponse.body()?.results
                            ?.firstOrNull { it.producto == productoId }?.id
                        if (favId != null) repo.removeFavorito(token, favId)
                    }
                }
            } catch (_: Exception) {
                // Error de red → revertir el estado optimista al snapshot anterior
                _favoritos.value = snapshotAntes
            } finally {
                // Sincronizar con el estado real del servidor (confirma o revierte)
                cargarFavoritos()
            }
        }
    }

    /** Búsqueda de productos para el selector de lista. No afecta _productos. */
    fun buscarProductosParaLista(query: String) {
        if (query.isBlank()) {
            _productosBusqueda.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                // Si la query parece un código de barras (número), buscar por ID primero
                val codigoBarras = query.trim().toLongOrNull()
                if (codigoBarras != null) {
                    val resp = repo.getProducto(token, codigoBarras)
                    if (resp.isSuccessful && resp.body() != null) {
                        _productosBusqueda.value = listOf(resp.body()!!.toUiProduct())
                        return@launch
                    }
                }
                // Búsqueda por texto (nombre / marca)
                val resp = repo.getProductos(token, search = query)
                if (resp.isSuccessful) {
                    _productosBusqueda.value = resp.body()?.results?.map { it.toUiProduct() } ?: emptyList()
                }
            } catch (_: Exception) { }
        }
    }

    fun limpiarBusquedaLista() {
        _productosBusqueda.value = emptyList()
    }

    fun buscarProductoPorBarcode(barcode: String) {
        val codigoBarras = barcode.trim().toLongOrNull() ?: run {
            _productoNoEncontrado.value = true
            return
        }
        viewModelScope.launch {
            try {
                val resp = repo.getProducto(token, codigoBarras)
                if (resp.isSuccessful && resp.body() != null) {
                    _productoEscaneado.value = resp.body()!!.toUiProduct()
                } else {
                    _productoNoEncontrado.value = true
                }
            } catch (_: Exception) {
                _productoNoEncontrado.value = true
            }
        }
    }

    fun limpiarProductoEscaneado() {
        _productoEscaneado.value = null
    }

    private fun com.example.tracksy.data.models.Producto.toUiProduct() =
        Product(id = id, name = nombre, category = marcaNombre ?: "", barcode = id.toString())

    private fun com.example.tracksy.data.models.ProductoDetalle.toUiProduct() =
        Product(id = id, name = nombre, category = marcaNombre ?: "", barcode = id.toString())

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductoViewModel(TracksyRepository(), TokenManager(context)) as T
    }
}
