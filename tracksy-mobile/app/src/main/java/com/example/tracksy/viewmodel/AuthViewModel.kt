package com.example.tracksy.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.repository.TracksyRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: TracksyRepository,
    val tokenManager: TokenManager
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(tokenManager.isLoggedIn())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    suspend fun login(email: String, password: String): Boolean {
        return try {
            val response = repo.login(email, password)
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.accessToken  = body.access
                tokenManager.refreshToken = body.refresh
                _isAuthenticated.value = true
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun registro(nombre: String, email: String, password: String): String? {
        val normalizedEmail = email.trim()
        return try {
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(normalizedEmail, password)
                .await()

            try {
                FirebaseAuth.getInstance()
                    .currentUser
                    ?.sendEmailVerification()
                    ?.await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Firebase email verification failed", e)
                return "La cuenta se creó, pero no se pudo enviar el email de verificación. Revisá la configuración de Firebase."
            }

            val idToken = FirebaseAuth.getInstance()
                .currentUser
                ?.getIdToken(false)
                ?.await()
                ?.token
                ?: return "La cuenta se creó, pero Firebase no devolvió un ID token."

            val syncResponse = repo.firebaseSync(idToken)
            if (!syncResponse.isSuccessful) {
                return "La cuenta se creó, pero Django rechazó el sync Firebase: HTTP ${syncResponse.code()}."
            }

            _isAuthenticated.value = true
            null
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e("AuthViewModel", "Firebase register failed: ${e.errorCode}", e)
            "Firebase rechazó el registro: ${e.errorCode}. Ese correo ya existe en Firebase Authentication."
        } catch (e: FirebaseAuthWeakPasswordException) {
            Log.e("AuthViewModel", "Firebase register failed: ${e.errorCode}", e)
            e.reason ?: "Firebase rechazó el registro: ${e.errorCode}"
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("AuthViewModel", "Firebase register failed: ${e.errorCode}", e)
            "Firebase rechazó el registro: ${e.errorCode}. El correo electrónico no es válido."
        } catch (e: FirebaseNetworkException) {
            "No se pudo conectar con Firebase. Revisá la conexión a internet."
        } catch (e: FirebaseAuthException) {
            Log.e("AuthViewModel", "Firebase register failed: ${e.errorCode}", e)
            when (e.errorCode) {
                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "El proveedor Email/Password no está habilitado en Firebase Console."
                "ERROR_APP_NOT_AUTHORIZED" ->
                    "La app no está autorizada en Firebase. Revisá package name, google-services.json y SHA."
                else ->
                    "Firebase rechazó el registro: ${e.errorCode}"
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Register failed", e)
            "No se pudo crear la cuenta. Revisá Logcat para ver el error de Firebase."
        }
    }

    fun logout() {
        tokenManager.clear()
        _isAuthenticated.value = false
    }

    val token: String get() = tokenManager.accessToken ?: ""

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(TracksyRepository(), TokenManager(context)) as T
    }
}
