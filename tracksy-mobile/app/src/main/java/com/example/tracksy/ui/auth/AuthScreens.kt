package com.example.tracksy.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private enum class AuthRoute {
    Welcome,
    Login
}

@Composable
fun TracksyAuthApp() {
    var route by remember { mutableStateOf(AuthRoute.Welcome) }

    when (route) {
        AuthRoute.Welcome -> WelcomeScreen(
            onCreateAccount = {
                // TODO: Navigate to create account flow when it is implemented.
            },
            onLogin = { route = AuthRoute.Login }
        )

        AuthRoute.Login -> LoginScreen(
            onLogin = { _, _ -> false },
            onForgotPassword = {
                // TODO: Navigate to forgot password flow when it is implemented.
            },
            onCreateAccount = {
                // TODO: Navigate to create account flow when it is implemented.
            }
        )
    }
}

@Composable
fun WelcomeScreen(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    TracksyAuthBackground(modifier = modifier, imageAlpha = 0.60f) {
        Box(modifier = Modifier.fillMaxSize()) {
            WelcomeBrand(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 88.dp, start = 32.dp, end = 32.dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 65.dp, end = 65.dp, bottom = 36.dp)
                    .widthIn(max = 254.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TracksyPrimaryButton(
                    text = "Crear cuenta",
                    onClick = onCreateAccount
                )
                TracksySecondaryButton(
                    text = "Iniciar sesión",
                    onClick = onLogin
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Boolean,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    LoginContent(
        email = email,
        password = password,
        showError = showError,
        onEmailChange = {
            email = it
            if (showError) showError = false
        },
        onPasswordChange = {
            password = it
            if (showError) showError = false
        },
        onLogin = {
            if (email.isNotBlank() && password.isNotBlank()) {
                showError = !onLogin(email, password)
            }
        },
        onForgotPassword = onForgotPassword,
        onCreateAccount = onCreateAccount,
        modifier = modifier
    )
}

@Composable
internal fun LoginContent(
    email: String,
    password: String,
    showError: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loginEnabled = email.isNotBlank() && password.isNotBlank()

    AuthScreenContainer(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 56.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader(title = "Iniciar sesión")
            Spacer(modifier = Modifier.height(57.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 254.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TracksyTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Correo electrónico",
                    keyboardType = KeyboardType.Email
                )
                Spacer(modifier = Modifier.height(20.dp))
                TracksyPasswordField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Contraseña"
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(13.dp))
                    ErrorMessage(text = "Correo electrónico o contraseña incorrectos")
                    Spacer(modifier = Modifier.height(35.dp))
                } else {
                    Spacer(modifier = Modifier.height(38.dp))
                }
                TracksyPrimaryButton(
                    text = "Ingresar",
                    onClick = onLogin,
                    enabled = loginEnabled
                )
                Spacer(modifier = Modifier.height(16.dp))
                TracksyLinkText(
                    text = "¿Olvidaste tu contraseña?",
                    onClick = onForgotPassword,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                TracksyInlineLink(
                    text = "¿No tenés cuenta?",
                    linkText = "Creá una",
                    onClick = onCreateAccount,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
