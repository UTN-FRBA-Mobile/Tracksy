package com.example.tracksy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.repository.TracksyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    suspend fun registro(nombre: String, email: String, password: String): Boolean {
        return try {
            val response = repo.registro(nombre, email, password)
            if (response.isSuccessful) {
                login(email, password)
            } else false
        } catch (e: Exception) {
            false
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
