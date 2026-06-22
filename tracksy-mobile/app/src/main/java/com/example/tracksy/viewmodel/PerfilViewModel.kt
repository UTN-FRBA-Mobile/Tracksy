package com.example.tracksy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.auth.FirebaseAuthService
import com.example.tracksy.data.repository.TracksyRepository
import com.example.tracksy.ui.profile.PerfilUsuario
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repo: TracksyRepository,
    private val firebaseAuthService: FirebaseAuthService
) : ViewModel() {

    private val _perfil = MutableStateFlow<PerfilUsuario?>(null)
    val perfil: StateFlow<PerfilUsuario?> = _perfil

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarPerfil() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repo.getPerfil()
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
                val response = repo.updatePerfil(nombre)
                if (response.isSuccessful) {
                    val u = response.body()!!
                    _perfil.value = PerfilUsuario(u.nombre, u.email)
                }
            } catch (_: Exception) { }
        }
    }

    suspend fun cambiarPassword(passwordActual: String, passwordNuevo: String): String? {
        return try {
            firebaseAuthService.changePassword(passwordActual, passwordNuevo)
            null
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            "La contraseña actual no es correcta."
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            "Por seguridad, volvé a iniciar sesión e intentá nuevamente."
        } catch (e: FirebaseNetworkException) {
            "No se pudo conectar con Firebase. Revisá la conexión a internet."
        } catch (e: FirebaseAuthException) {
            "Firebase rechazó el cambio de contraseña: ${e.errorCode}"
        } catch (e: Exception) {
            "No se pudo cambiar la contraseña."
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PerfilViewModel(TracksyRepository(), FirebaseAuthService()) as T
    }
}
