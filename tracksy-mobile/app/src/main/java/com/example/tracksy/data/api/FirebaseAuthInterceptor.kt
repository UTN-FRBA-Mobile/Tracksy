package com.example.tracksy.data.api

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Interceptor
import okhttp3.Response

class FirebaseAuthInterceptor(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val user = firebaseAuth.currentUser ?: return chain.proceed(request)
        // getIdToken() puede pegarle a la red de Firebase (fuera del OkHttpClient y sus
        // timeouts) para refrescar el token, sobre todo justo después del primer login.
        // Sin límite acá, ese refresh puede tardar indefinidamente y dejar el runBlocking
        // colgado para siempre, congelando la UI en el estado "conectando".
        val idToken = runBlocking {
            withTimeoutOrNull(10_000) {
                runCatching { user.getIdToken(false).await()?.token }.getOrNull()
            }
        } ?: return chain.proceed(request)

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $idToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
