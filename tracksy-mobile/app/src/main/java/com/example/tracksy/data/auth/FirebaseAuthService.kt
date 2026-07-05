package com.example.tracksy.data.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun register(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        if (name.isNotBlank()) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name.trim())
                .build()
            auth.currentUser?.updateProfile(profileUpdates)?.await()
        }
        sendEmailVerification()
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: error("No authenticated Firebase user.")
        val email = user.email ?: error("Authenticated Firebase user has no email.")
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    suspend fun updateDisplayName(name: String) {
        val user = auth.currentUser ?: error("No authenticated Firebase user.")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name.trim())
            .build()
        user.updateProfile(profileUpdates).await()
    }

    suspend fun getIdToken(forceRefresh: Boolean = false): String {
        val user = auth.currentUser ?: error("No authenticated Firebase user.")
        return user.getIdToken(forceRefresh).await().token
            ?: error("Firebase did not return an ID token.")
    }

    suspend fun reloadCurrentUser() {
        auth.currentUser?.reload()?.await()
    }

    fun isAuthenticated(): Boolean = auth.currentUser != null

    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified == true

    fun currentEmail(): String = auth.currentUser?.email.orEmpty()

    fun currentDisplayName(): String = auth.currentUser?.displayName.orEmpty()
}
