package com.example.tracksy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.models.EstadoProducto
import com.example.tracksy.data.models.ListaCompra
import com.example.tracksy.data.models.Supermercado
import com.example.tracksy.data.repository.TracksyRepository
import com.example.tracksy.screens.ShoppingList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListaViewModel(
    private val repo: TracksyRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _listas = MutableStateFlow<List<ShoppingList>>(emptyList())
    val listas: StateFlow<List<ShoppingList>> = _listas

    private val _listaActual = MutableStateFlow<ListaCompra?>(null)
    val listaActual: StateFlow<ListaCompra?> = _listaActual

    private val _estadosProducto = MutableStateFlow<List<EstadoProducto>>(emptyList())
    val estadosProducto: StateFlow<List<EstadoProducto>> = _estadosProducto

    private val _supermercados = MutableStateFlow<List<Supermercado>>(emptyList())
    val supermercados: StateFlow<List<Supermercado>> = _supermercados

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val token get() = tokenManager.accessToken ?: ""

    // ── Estado helpers ─────────────────────────────────────────────────────────

    fun idEstadoPendiente(): Int? =
        _estadosProducto.value.firstOrNull { it.nombre.lowercase().contains("pendiente") }?.id

    fun idEstadoComprado(): Int? =
        _estadosProducto.value.firstOrNull {
            it.nombre.lowercase().contains("comprad") || it.nombre.lowercase().contains("comprado")
        }?.id

    fun estadoEsComprado(estadoNombre: String): Boolean =
        estadoNombre.lowercase().contains("comprad")

    // ── Load ──────────────────────────────────────────────────────────────────

    fun cargarListas() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repo.getListas(token)
                if (response.isSuccessful) {
                    _listas.value = response.body()?.results?.map { it.toUiModel() } ?: emptyList()
                }
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun cargarLista(id: Int) {
        viewModelScope.launch {
            try {
                val response = repo.getLista(token, id)
                if (response.isSuccessful) {
                    _listaActual.value = response.body()
                }
            } catch (_: Exception) { }
        }
    }

    fun cargarEstadosProducto() {
        viewModelScope.launch {
            try {
                val response = repo.getEstadosProducto(token)
                if (response.isSuccessful) {
                    _estadosProducto.value = response.body()?.results ?: emptyList()
                }
            } catch (_: Exception) { }
        }
    }

    fun cargarSupermercados() {
        viewModelScope.launch {
            try {
                val response = repo.getSupermercados(token)
                if (response.isSuccessful) {
                    _supermercados.value = response.body()?.results ?: emptyList()
                }
            } catch (_: Exception) { }
        }
    }

    // ── Crear lista con items ─────────────────────────────────────────────────

    fun crearListaConItems(
        nombre: String,
        supermercadoId: Int?,
        items: List<Pair<Int, Int>>  // (productoId, cantidad)
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val listaResp = repo.crearLista(token, nombre, supermercadoId)
                if (listaResp.isSuccessful) {
                    val listaId = listaResp.body()!!.id
                    val estadoId = idEstadoPendiente()
                        ?: _estadosProducto.value.firstOrNull()?.id
                        ?: return@launch
                    items.forEach { (productoId, cantidad) ->
                        repo.agregarItem(token, listaId, productoId, cantidad, estadoId, 0.0)
                    }
                    cargarListas()
                }
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun crearLista(nombre: String, supermercadoId: Int? = null) {
        viewModelScope.launch {
            try {
                val response = repo.crearLista(token, nombre, supermercadoId)
                if (response.isSuccessful) cargarListas()
            } catch (_: Exception) { }
        }
    }

    // ── Item actions ──────────────────────────────────────────────────────────

    fun eliminarLista(id: Int) {
        viewModelScope.launch {
            try {
                repo.deleteLista(token, id)
                cargarListas()
            } catch (_: Exception) { }
        }
    }

    fun agregarItem(listaId: Int, productoId: Int, cantidad: Int, estadoId: Int, precio: Double) {
        viewModelScope.launch {
            try {
                val response = repo.agregarItem(token, listaId, productoId, cantidad, estadoId, precio)
                if (response.isSuccessful) cargarLista(listaId)
            } catch (_: Exception) { }
        }
    }

    fun eliminarItem(listaId: Int, itemId: Int) {
        viewModelScope.launch {
            try {
                repo.eliminarItem(token, listaId, itemId)
                cargarLista(listaId)
            } catch (_: Exception) { }
        }
    }

    fun marcarItem(listaId: Int, itemId: Int, estadoId: Int) {
        viewModelScope.launch {
            try {
                repo.updateItem(token, listaId, itemId, mapOf("estado" to estadoId))
                cargarLista(listaId)
            } catch (_: Exception) { }
        }
    }

    fun toggleItem(listaId: Int, itemId: Int, estaComprado: Boolean) {
        val nuevoEstadoId = if (estaComprado) idEstadoPendiente() else idEstadoComprado()
        nuevoEstadoId?.let { marcarItem(listaId, itemId, it) }
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private fun ListaCompra.toUiModel(): ShoppingList {
        val total = items.size
        val pendientes = items.count { !estadoEsComprado(it.estadoNombre) }
        return ShoppingList(id, nombre, total, pendientes)
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ListaViewModel(TracksyRepository(), TokenManager(context)) as T
    }
}
