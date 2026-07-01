package com.example.tracksy.viewmodel

import com.example.tracksy.data.auth.FirebaseAuthService
import com.example.tracksy.data.local.TokenManager
import com.example.tracksy.data.models.FirebaseSyncResponse
import com.example.tracksy.data.repository.TracksyRepositoryInterface
import com.example.tracksy.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: TracksyRepositoryInterface
    private lateinit var authService: FirebaseAuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        repo = mockk()
        authService = mockk()
        tokenManager = mockk()

        every { authService.isAuthenticated() } returns false
        every { tokenManager.pendingEmailVerification } returns false

        viewModel = AuthViewModel(repo, authService, tokenManager)
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    fun `login setea isAuthenticated en true cuando Firebase y el sync son exitosos`() = runTest {
        coEvery { authService.login(any(), any()) } just Runs
        coEvery { repo.firebaseSync() } returns Response.success(syncResponseFake())
        every { tokenManager.pendingEmailVerification = false } just Runs

        val error = viewModel.login("user@test.com", "Pass123!")

        assertNull(error)
        assertTrue(viewModel.isAuthenticated.value)
    }

    @Test
    fun `login retorna mensaje de error cuando las credenciales son invalidas`() = runTest {
        val exception = mockk<FirebaseAuthInvalidCredentialsException>()
        every { exception.errorCode } returns "ERROR_INVALID_CREDENTIAL"
        coEvery { authService.login(any(), any()) } throws exception

        val error = viewModel.login("user@test.com", "wrongpass")

        assertNotNull(error)
        assertTrue(error!!.contains("incorrectos"))
        assertFalse(viewModel.isAuthenticated.value)
    }

    @Test
    fun `login retorna mensaje de error cuando el sync con Django falla`() = runTest {
        coEvery { authService.login(any(), any()) } just Runs
        coEvery { repo.firebaseSync() } returns Response.error(
            400, "Bad request".toResponseBody()
        )

        val error = viewModel.login("user@test.com", "Pass123!")

        assertNotNull(error)
        assertTrue(error!!.contains("sync Firebase"))
        assertFalse(viewModel.isAuthenticated.value)
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    fun `logout setea isAuthenticated en false`() = runTest {
        coEvery { authService.login(any(), any()) } just Runs
        coEvery { repo.firebaseSync() } returns Response.success(syncResponseFake())
        every { tokenManager.pendingEmailVerification = false } just Runs
        viewModel.login("user@test.com", "Pass123!")

        every { authService.logout() } just Runs
        every { tokenManager.pendingEmailVerification = false } just Runs

        viewModel.logout()

        assertFalse(viewModel.isAuthenticated.value)
    }

    // ── registro ──────────────────────────────────────────────────────────────

    @Test
    fun `registro setea pendingEmailVerification en true si Firebase y sync son exitosos`() = runTest {
        coEvery { authService.register(any(), any(), any()) } just Runs
        coEvery { repo.firebaseSync() } returns Response.success(syncResponseFake())
        every { tokenManager.pendingEmailVerification = true } just Runs

        val error = viewModel.registro("Juan", "juan@test.com", "Pass123!")

        assertNull(error)
        verify(exactly = 1) { tokenManager.pendingEmailVerification = true }
        assertFalse(viewModel.isAuthenticated.value)
    }

    // ── refreshEmailVerification ──────────────────────────────────────────────

    @Test
    fun `refreshEmailVerification retorna false si el email todavia no esta verificado`() = runTest {
        coEvery { authService.reloadCurrentUser() } just Runs
        every { authService.isEmailVerified() } returns false

        val result = viewModel.refreshEmailVerification()

        assertFalse(result)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun syncResponseFake() = FirebaseSyncResponse(
        id = "1",
        firebaseUid = "uid123",
        email = "user@test.com",
        isEmailVerified = true
    )
}
