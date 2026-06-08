package com.example.tracksy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.models.Usuario
import com.example.tracksy.data.repository.TracksyRepository
import com.example.tracksy.ui.profile.PerfilUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repo: TracksyRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _perfil = MutableStateFlow<PerfilUsuario?>(null)
    val perfil: StateFlow<PerfilUsuario?> = _perfil

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarPerfil() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repo.getPerfil(tokenManager.accessToken ?: "")
                if (response.isSuccessful) {
                    val u = response.body()!!
                    _perfil.value = PerfilUsuario(u.nombre, u.email)
                }
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun actualizarPerfil(nombre: String) {
        viewModelScope.launch {
            try {
                val response = repo.updatePerfil(tokenManager.accessToken ?: "", nombre)
                if (response.isSuccessful) {
                    val u = response.body()!!
                    _perfil.value = PerfilUsuario(u.nombre, u.email)
                }
            } catch (_: Exception) { }
        }
    }

    suspend fun cambiarPassword(passwordActual: String, passwordNuevo: String): Boolean {
        return try {
            val response = repo.cambiarPassword(
                tokenManager.accessToken ?: "",
                passwordActual,
                passwordNuevo
            )
            response.isSuccessful
        } catch (_: Exception) { false }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PerfilViewModel(TracksyRepository(), TokenManager(context)) as T
    }
}
