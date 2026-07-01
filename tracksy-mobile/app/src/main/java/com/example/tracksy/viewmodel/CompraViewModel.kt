package com.example.tracksy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.models.Compra
import com.example.tracksy.data.models.CompraRequest
import com.example.tracksy.data.models.ProductoCompradoRequest
import com.example.tracksy.data.repository.TracksyRepository
import com.example.tracksy.data.repository.TracksyRepositoryInterface
import com.example.tracksy.ui.history.HistoryItem
import com.example.tracksy.ui.history.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CompraViewModel(
    private val repo: TracksyRepositoryInterface
) : ViewModel() {

    private val _compras = MutableStateFlow<List<HistoryItem>>(emptyList())
    val compras: StateFlow<List<HistoryItem>> = _compras

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Exposed as StateFlow for pull-to-refresh indicator compatibility
    val isRefreshing: StateFlow<Boolean> = _isLoading
    fun cargarCompras() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repo.getCompras()
                if (response.isSuccessful) {
                    _compras.value = response.body()?.results?.map { it.toHistoryItem() } ?: emptyList()
                }
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun crearCompra(
        supermercadoId: Int?,
        nombreLista: String,
        total: Double,
        productos: List<Triple<Long, Int, Double>>   // (productoId EAN-13, cantidad, precioUnitario)
    ) {
        viewModelScope.launch {
            try {
                val request = CompraRequest(
                    supermercado = supermercadoId,
                    nombreLista  = nombreLista,
                    total        = total,
                    productos    = productos.map { (id, cant, precio) ->
                        ProductoCompradoRequest(id, cant, precio)
                    }
                )
                val response = repo.crearCompra(request)
                if (response.isSuccessful) cargarCompras()
            } catch (_: Exception) { }
        }
    }

    private fun Compra.toHistoryItem(): HistoryItem {
        val fechaLabel = try {
            val datePart = fecha.substring(0, 10)
            SimpleDateFormat("d 'de' MMMM", Locale("es", "AR"))
                .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(datePart)!!)
        } catch (_: Exception) { fecha }

        return HistoryItem(
            id              = id.toString(),
            listName        = nombreLista.ifBlank { "Compra" },
            supermarketName = supermercadoNombre ?: "",
            dateLabel       = fechaLabel,
            productCount    = productos.size,
            totalAmount     = "$%.0f".format(total),
            products        = productos.map { pc ->
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
            CompraViewModel(TracksyRepository()) as T
    }
}
