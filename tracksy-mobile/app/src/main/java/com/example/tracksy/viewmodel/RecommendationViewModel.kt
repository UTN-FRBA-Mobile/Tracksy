package com.example.tracksy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.local.RecommendationStorage
import com.example.tracksy.data.repository.TracksyRepository
import com.example.tracksy.recommendations.Recommendation
import com.example.tracksy.recommendations.RecommendationContext
import com.example.tracksy.recommendations.RecommendationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val storage: RecommendationStorage,
    private val repo: TracksyRepository,
    private val engine: RecommendationEngine = RecommendationEngine()
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _recommendations.value = storage.loadVisible()
        }
    }

    // Fetches fresh data from the API, runs all criteria, persists results,
    // and updates the observable list. Runs on IO to keep the main thread free.
    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val favoritos = repo.getFavoritos().body()?.results ?: emptyList()
                val productos = repo.getProductos(pageSize = 200).body()?.results ?: emptyList()
                val compras = repo.getCompras().body()?.results ?: emptyList()
                val listas = repo.getListas().body()?.results ?: emptyList()

                val ctx = RecommendationContext(
                    favoritos = favoritos,
                    productos = productos,
                    compras = compras,
                    listasActivas = listas
                )
                storage.mergeAndSave(engine.evaluate(ctx))
            }
            _recommendations.value = storage.loadVisible()
        }
    }

    fun dismiss(productoId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            storage.markDismissed(productoId)
            _recommendations.value = storage.loadVisible()
        }
    }

    fun clearOnLogout() {
        viewModelScope.launch(Dispatchers.IO) {
            storage.clear()
            _recommendations.value = emptyList()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RecommendationViewModel(
                RecommendationStorage(context),
                TracksyRepository()
            ) as T
    }
}
