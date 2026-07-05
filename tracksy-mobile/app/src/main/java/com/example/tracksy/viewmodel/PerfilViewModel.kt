package com.example.tracksy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.auth.FirebaseAuthService
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.repository.TracksyRepository
import com.example.tracksy.data.repository.TracksyRepositoryInterface
import com.example.tracksy.ui.profile.PerfilUsuario
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repo: TracksyRepositoryInterface,
    private val firebaseAuthService: FirebaseAuthService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _perfil = MutableStateFlow(firebaseProfile())
    val perfil: StateFlow<PerfilUsuario?> = _perfil

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarPerfil() {
        viewModelScope.launch {
            _isLoading.value = true
            _perfil.value = firebaseProfile()
            try {
                val response = repo.getPerfil()
                if (response.isSuccessful) {
                    val backendName = response.body()?.nombre.orEmpty()
                    _perfil.value = firebaseProfile(backendName)
                }
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun actualizarPerfil(nombre: String, fotoUri: String) {
        viewModelScope.launch {
            val trimmedName = nombre.trim()
            tokenManager.profilePhotoUri = fotoUri
            try {
                firebaseAuthService.updateDisplayName(trimmedName)
                _perfil.value = firebaseProfile(trimmedName)

                val response = repo.updatePerfil(trimmedName)
                if (response.isSuccessful) _perfil.value = firebaseProfile(response.body()?.nombre.orEmpty())
            } catch (_: Exception) { }
        }
    }

    private fun firebaseProfile(preferredName: String = ""): PerfilUsuario {
        val email = firebaseAuthService.currentEmail()
        val name = preferredName
            .ifBlank { firebaseAuthService.currentDisplayName() }
            .ifBlank { email.substringBefore("@", missingDelimiterValue = "") }
        return PerfilUsuario(nombre = name, email = email, fotoUri = tokenManager.profilePhotoUri)
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
            PerfilViewModel(TracksyRepository(), FirebaseAuthService(), TokenManager(context)) as T
    }
}
