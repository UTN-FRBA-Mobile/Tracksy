package com.example.tracksy.data.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response

class FirebaseAuthInterceptor(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val user = firebaseAuth.currentUser ?: return chain.proceed(request)
        val idToken = Tasks.await(user.getIdToken(false)).token ?: return chain.proceed(request)

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $idToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
