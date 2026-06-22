package com.example.tracksy.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tracksy.data.auth.FirebaseAuthService
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.repository.TracksyRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(
    private val repo: TracksyRepository,
    private val firebaseAuthService: FirebaseAuthService,
    val tokenManager: TokenManager
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(firebaseAuthService.isAuthenticated())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    suspend fun login(email: String, password: String): Boolean {
        return authenticateWithFirebase {
            firebaseAuthService.login(email, password)
        } == null
    }

    suspend fun registro(nombre: String, email: String, password: String): String? {
        return authenticateWithFirebase {
            firebaseAuthService.register(nombre, email, password)
        }
    }

    private suspend fun authenticateWithFirebase(authAction: suspend () -> Unit): String? {
        return try {
            authAction()
            syncFirebaseUser()
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e("AuthViewModel", "Firebase register failed: ${e.errorCode}", e)
            "Ya existe una cuenta con este correo. Iniciá sesión o recuperá tu contraseña."
        } catch (e: FirebaseAuthWeakPasswordException) {
            Log.e("AuthViewModel", "Firebase register failed: ${e.errorCode}", e)
            e.reason ?: "Firebase rechazó el registro: ${e.errorCode}"
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("AuthViewModel", "Firebase register failed: ${e.errorCode}", e)
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL",
                "ERROR_INVALID_EMAIL",
                "ERROR_WRONG_PASSWORD" ->
                    "El correo o la contraseña son incorrectos."
                else ->
                    "Firebase rechazó la autenticación: ${e.errorCode}"
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("AuthViewModel", "Firebase auth failed: ${e.errorCode}", e)
            "No existe una cuenta Firebase activa para ese correo."
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

    private suspend fun syncFirebaseUser(): String? {
        val idToken = firebaseAuthService.getIdToken()
        val syncResponse = repo.firebaseSync(idToken)
        if (!syncResponse.isSuccessful) {
            return "Django rechazó el sync Firebase: HTTP ${syncResponse.code()}."
        }

        tokenManager.accessToken = idToken
        tokenManager.refreshToken = null
        _isAuthenticated.value = true
        return null
    }

    fun logout() {
        firebaseAuthService.logout()
        tokenManager.clear()
        _isAuthenticated.value = false
    }

    suspend fun sendPasswordReset(email: String): String? {
        return try {
            firebaseAuthService.sendPasswordReset(email)
            null
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("AuthViewModel", "Firebase password reset failed: ${e.errorCode}", e)
            "Ingresá un correo electrónico válido."
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("AuthViewModel", "Firebase password reset failed: ${e.errorCode}", e)
            "No existe una cuenta Firebase activa para ese correo."
        } catch (e: FirebaseNetworkException) {
            "No se pudo conectar con Firebase. Revisá la conexión a internet."
        } catch (e: FirebaseAuthException) {
            Log.e("AuthViewModel", "Firebase password reset failed: ${e.errorCode}", e)
            "Firebase rechazó el recupero de contraseña: ${e.errorCode}"
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Password reset failed", e)
            "No se pudo enviar el email de recuperación. Revisá Logcat para ver el error."
        }
    }

    val token: String get() = tokenManager.accessToken ?: ""

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(TracksyRepository(), FirebaseAuthService(), TokenManager(context)) as T
    }
}
