package com.example.tracksy.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.tracksy.ui.theme.TracksyTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Test 1: botón deshabilitado con campos vacíos ─────────────────────────

    @Test
    fun boton_ingresar_deshabilitado_con_campos_vacios() {
        var loginLlamado = false

        composeTestRule.setContent {
            TracksyTheme {
                LoginContent(
                    email = "",
                    password = "",
                    errorText = null,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onLogin = { loginLlamado = true },
                    onForgotPassword = {},
                    onCreateAccount = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Ingresar").performClick()
        assertFalse(loginLlamado)
    }

    // ── Test 2: botón habilitado con ambos campos completos ───────────────────

    @Test
    fun boton_ingresar_habilitado_con_email_y_password_completos() {
        var loginLlamado = false

        composeTestRule.setContent {
            TracksyTheme {
                LoginContent(
                    email = "test@email.com",
                    password = "password123",
                    errorText = null,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onLogin = { loginLlamado = true },
                    onForgotPassword = {},
                    onCreateAccount = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Ingresar").performClick()
        assertTrue(loginLlamado)
    }

    // ── Test 3 (original): muestra el error del servidor ─────────────────────

    @Test
    fun muestra_mensaje_de_error_del_servidor() {
        val mensajeError = "El correo o la contraseña son incorrectos."

        composeTestRule.setContent {
            TracksyTheme {
                LoginContent(
                    email = "test@email.com",
                    password = "wrongpass",
                    errorText = mensajeError,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onLogin = {},
                    onForgotPassword = {},
                    onCreateAccount = {}
                )
            }
        }

        composeTestRule.onNodeWithText(mensajeError).assertIsDisplayed()
    }
}
