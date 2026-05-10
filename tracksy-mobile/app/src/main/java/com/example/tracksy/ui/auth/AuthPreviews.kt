package com.example.tracksy.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.tracksy.ui.theme.TracksyTheme

@Preview(name = "Welcome Screen", showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    TracksyTheme {
        WelcomeScreen(
            onCreateAccount = {},
            onLogin = {}
        )
    }
}

@Preview(name = "Login Empty", showBackground = true)
@Composable
private fun LoginEmptyPreview() {
    TracksyTheme {
        LoginScreen(
            onLogin = { _, _ -> false },
            onForgotPassword = {},
            onCreateAccount = {}
        )
    }
}

@Preview(name = "Auth Components", showBackground = true)
@Composable
private fun AuthComponentsPreview() {
    LoginEmptyPreview()
}

@Preview(name = "Recover Password Empty", showBackground = true)
@Composable
private fun RecoverPasswordEmptyPreview() {
    TracksyTheme {
        RecoverPasswordScreen(
            onBack = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "Recover Password Valid Email", showBackground = true)
@Composable
private fun RecoverPasswordValidPreview() {
    TracksyTheme {
        RecoverPasswordContent(
            email = "juan.perez@gmail.com",
            showEmailError = false,
            onEmailChange = {},
            onEmailFocusChanged = {},
            onBack = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "Recover Password Invalid Email", showBackground = true)
@Composable
private fun RecoverPasswordInvalidPreview() {
    TracksyTheme {
        RecoverPasswordContent(
            email = "juan.perez@gmail",
            showEmailError = true,
            onEmailChange = {},
            onEmailFocusChanged = {},
            onBack = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "Check Email Success", showBackground = true)
@Composable
private fun CheckEmailSuccessPreview() {
    TracksyTheme {
        CheckEmailScreen(
            onBack = {},
            onBackToLogin = {},
            onResend = {}
        )
    }
}
