package com.example.tracksy.viewmodel

import android.content.Context
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val token get() = tokenManager.accessToken ?: ""

    fun cargarProductos(busqueda: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repo.getProductos(token, search = busqueda)
                if (response.isSuccessful) {
                    _productos.value = response.body()?.results?.map { it.toUiProduct() } ?: emptyList()
                }
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun cargarFavoritos() {
        viewModelScope.launch {
            try {
                val favResponse = repo.getFavoritos(token)
                if (favResponse.isSuccessful) {
                    val productoIds = favResponse.body()?.results?.map { it.producto } ?: return@launch
                    val productosResp = repo.getProductos(token)
                    if (productosResp.isSuccessful) {
                        val todos = productosResp.body()?.results ?: emptyList()
                        _favoritos.value = todos
                            .filter { it.id in productoIds }
                            .map { it.toUiProduct() }
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun toggleFavorito(productoId: Int, esFavorito: Boolean) {
        viewModelScope.launch {
            try {
                if (esFavorito) {
                    repo.addFavorito(token, productoId)
                } else {
                    val favResponse = repo.getFavoritos(token)
                    val favId = favResponse.body()?.results
                        ?.firstOrNull { it.producto == productoId }?.id
                    if (favId != null) repo.removeFavorito(token, favId)
                }
                cargarFavoritos()
            } catch (_: Exception) { }
        }
    }

    private fun com.example.tracksy.data.models.Producto.toUiProduct() =
        Product(id = id, name = nombre, category = marcaNombre ?: "", barcode = null)

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductoViewModel(TracksyRepository(), TokenManager(context)) as T
    }
}
