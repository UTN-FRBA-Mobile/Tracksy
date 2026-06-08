package com.example.tracksy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.models.Estado
import com.example.tracksy.data.models.Sugerencia
import com.example.tracksy.data.repository.TracksyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SugerenciaViewModel(
    private val repo: TracksyRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _sugerencias = MutableStateFlow<List<Sugerencia>>(emptyList())
    val sugerencias: StateFlow<List<Sugerencia>> = _sugerencias

    private val _estados = MutableStateFlow<List<Estado>>(emptyList())
    val estados: StateFlow<List<Estado>> = _estados

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val token get() = tokenManager.accessToken ?: ""

    fun cargarSugerencias() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repo.getSugerencias(token)
                if (response.isSuccessful) {
                    _sugerencias.value = response.body()?.results ?: emptyList()
                } else {
                    _error.value = "Error al cargar sugerencias (${response.code()})"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            }
            _isLoading.value = false
        }
    }

    fun cargarEstados() {
        viewModelScope.launch {
            try {
                val response = repo.getEstados(token)
                if (response.isSuccessful) {
                    _estados.value = response.body()?.results ?: emptyList()
                }
            } catch (_: Exception) { }
        }
    }

    fun crearSugerencia(productoId: Int, motivo: String) {
        viewModelScope.launch {
            val estadoId = _estados.value.firstOrNull()?.id ?: return@launch
            try {
                val response = repo.crearSugerencia(token, productoId, estadoId, motivo)
                if (response.isSuccessful) {
                    cargarSugerencias()
                }
            } catch (_: Exception) { }
        }
    }

    fun agregarFeedback(sugerenciaId: Int, fueUtil: Boolean) {
        viewModelScope.launch {
            try {
                repo.agregarFeedback(token, sugerenciaId, fueUtil)
                cargarSugerencias()
            } catch (_: Exception) { }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SugerenciaViewModel(TracksyRepository(), TokenManager(context)) as T
    }
}
