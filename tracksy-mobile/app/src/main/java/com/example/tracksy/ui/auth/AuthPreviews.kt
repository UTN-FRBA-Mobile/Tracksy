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

@Preview(name = "Create Account Empty", showBackground = true)
@Composable
private fun CreateAccountEmptyPreview() {
    TracksyTheme {
        CreateAccountPreviewContent()
    }
}

@Preview(name = "Create Account Invalid Email", showBackground = true)
@Composable
private fun CreateAccountInvalidEmailPreview() {
    TracksyTheme {
        CreateAccountPreviewContent(
            name = "Juan",
            email = "juan.perez@gmail",
            password = "*3",
            showPasswordRequirements = true,
            emailErrorText = "Ingresá un correo electrónico válido"
        )
    }
}

@Preview(name = "Create Account Password Checklist Neutral", showBackground = true)
@Composable
private fun CreateAccountPasswordChecklistNeutralPreview() {
    TracksyTheme {
        CreateAccountPreviewContent(
            name = "Juan",
            email = "juan.perez@gmail.com",
            password = "mobile123",
            showPasswordRequirements = true
        )
    }
}

@Preview(name = "Create Account Password Checklist Errors", showBackground = true)
@Composable
private fun CreateAccountPasswordChecklistErrorsPreview() {
    TracksyTheme {
        CreateAccountPreviewContent(
            name = "Juan",
            email = "juan.perez@gmail.com",
            password = "mobile123",
            showPasswordRequirements = true,
            showPasswordRequirementErrors = true
        )
    }
}

@Preview(name = "Create Account Password Valid", showBackground = true)
@Composable
private fun CreateAccountPasswordValidPreview() {
    TracksyTheme {
        CreateAccountPreviewContent(
            name = "Juan",
            email = "juan.perez@gmail.com",
            password = "Mobile*123",
            showPasswordRequirements = true
        )
    }
}

@Preview(name = "Create Account Password Mismatch", showBackground = true)
@Composable
private fun CreateAccountPasswordMismatchPreview() {
    TracksyTheme {
        CreateAccountPreviewContent(
            name = "Juan",
            email = "juan.perez@gmail.com",
            password = "Mobile*123",
            confirmPassword = "Mobile*124",
            showPasswordRequirements = true,
            showConfirmMismatch = true
        )
    }
}

@Preview(name = "Create Account Email Already Registered", showBackground = true)
@Composable
private fun CreateAccountEmailAlreadyRegisteredPreview() {
    TracksyTheme {
        CreateAccountPreviewContent(
            name = "Juan",
            email = "juan.perez@gmail.com",
            password = "Mobile*123",
            confirmPassword = "Mobile*123",
            emailErrorText = "Firebase rechazó el registro: ERROR_EMAIL_ALREADY_IN_USE",
            showPasswordRequirements = true
        )
    }
}

@Preview(name = "Create Account Fully Valid", showBackground = true)
@Composable
private fun CreateAccountFullyValidPreview() {
    TracksyTheme {
        CreateAccountPreviewContent(
            name = "Juan",
            email = "juan.perez@gmail.com",
            password = "Mobile*123",
            confirmPassword = "Mobile*123",
            showPasswordRequirements = true,
            isCreateEnabled = true
        )
    }
}

@Composable
private fun CreateAccountPreviewContent(
    name: String = "",
    email: String = "",
    password: String = "",
    confirmPassword: String = "",
    emailErrorText: String? = null,
    showPasswordRequirements: Boolean = false,
    showPasswordRequirementErrors: Boolean = false,
    showConfirmMismatch: Boolean = false,
    isCreateEnabled: Boolean = false
) {
    CreateAccountContent(
        name = name,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        emailErrorText = emailErrorText,
        showPasswordRequirements = showPasswordRequirements,
        passwordRequirements = passwordRequirements(password),
        showPasswordRequirementErrors = showPasswordRequirementErrors,
        showConfirmMismatch = showConfirmMismatch,
        isCreateEnabled = isCreateEnabled,
        onNameChange = {},
        onEmailChange = {},
        onEmailFocusChanged = {},
        onPasswordChange = {},
        onPasswordFocusChanged = {},
        onConfirmPasswordChange = {},
        onSubmit = {},
        onLogin = {}
    )
}
