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
