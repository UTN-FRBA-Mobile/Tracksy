package com.example.tracksy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.models.Compra
import com.example.tracksy.data.models.CompraRequest
import com.example.tracksy.data.models.ProductoCompradoRequest
import com.example.tracksy.data.repository.TracksyRepository
import com.example.tracksy.ui.history.HistoryItem
import com.example.tracksy.ui.history.Product
import com.example.tracksy.ui.history.PurchaseStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CompraViewModel(
    private val repo: TracksyRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _compras = MutableStateFlow<List<HistoryItem>>(emptyList())
    val compras: StateFlow<List<HistoryItem>> = _compras

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val token get() = tokenManager.accessToken ?: ""

    fun cargarCompras() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repo.getCompras(token)
                if (response.isSuccessful) {
                    _compras.value = response.body()?.results?.map { it.toHistoryItem() } ?: emptyList()
                }
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun crearCompra(
        supermercadoId: Int,
        total: Double,
        productos: List<Triple<Int, Int, Double>>   // (productoId, cantidad, precioUnitario)
    ) {
        viewModelScope.launch {
            try {
                val request = CompraRequest(
                    supermercado = supermercadoId,
                    total = total,
                    productos = productos.map { (id, cant, precio) ->
                        ProductoCompradoRequest(id, cant, precio)
                    }
                )
                val response = repo.crearCompra(token, request)
                if (response.isSuccessful) cargarCompras()
            } catch (_: Exception) { }
        }
    }

    private val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("d 'de' MMMM", Locale("es", "AR"))

    private fun Compra.toHistoryItem(): HistoryItem {
        val fechaLabel = try {
            dateFormatter.format(dateParser.parse(fecha)!!)
        } catch (_: Exception) { fecha }

        return HistoryItem(
            id            = id.toString(),
            supermarketName = supermercadoNombre,
            dateLabel     = fechaLabel,
            productCount  = productos.size,
            totalAmount   = "$%.0f".format(total),
            status        = PurchaseStatus.COMPLETED,
            products      = productos.map { pc ->
                Product(
                    name        = pc.productoNombre,
                    price       = "$%.0f".format(pc.precioUnitario * pc.cantidad),
                    isCompleted = true
                )
            }
        )
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CompraViewModel(TracksyRepository(), TokenManager(context)) as T
    }
}
