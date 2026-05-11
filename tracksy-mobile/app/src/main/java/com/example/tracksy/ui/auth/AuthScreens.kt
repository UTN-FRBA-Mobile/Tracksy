package com.example.tracksy.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.TracksyPrimaryPurple
import com.example.tracksy.ui.theme.TracksyChecklistNeutral
import com.example.tracksy.ui.theme.TracksyErrorRed
import com.example.tracksy.ui.theme.TracksySuccessGreen
import com.example.tracksy.ui.theme.TracksyTextMuted
import com.example.tracksy.ui.theme.TracksyTextSecondary

private enum class AuthRoute {
    Welcome,
    Login,
    CreateAccount,
    RecoverPassword,
    CheckEmail
}

private val AuthBodyTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.sp
)

private val PasswordRequirementTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 12.sp,
    letterSpacing = 0.sp
)

@Composable
fun TracksyAuthApp() {
    var route by remember { mutableStateOf(AuthRoute.Welcome) }

    when (route) {
        AuthRoute.Welcome -> WelcomeScreen(
            onCreateAccount = { route = AuthRoute.CreateAccount },
            onLogin = { route = AuthRoute.Login }
        )

        AuthRoute.Login -> LoginScreen(
            onLogin = { _, _ -> false },
            onForgotPassword = { route = AuthRoute.RecoverPassword },
            onCreateAccount = { route = AuthRoute.CreateAccount }
        )

        AuthRoute.CreateAccount -> CreateAccountScreen(
            onCreateAccount = { _, email, _ ->
                // TODO: Replace with backend auth registration when available.
                email != CreateAccountSampleRegisteredEmail
            },
            onLogin = { route = AuthRoute.Login }
        )

        AuthRoute.RecoverPassword -> RecoverPasswordScreen(
            onBack = { route = AuthRoute.Login },
            onSubmit = {
                // TODO: Send recover password instructions when backend is available.
                route = AuthRoute.CheckEmail
            }
        )

        AuthRoute.CheckEmail -> CheckEmailScreen(
            onBack = { route = AuthRoute.RecoverPassword },
            onBackToLogin = { route = AuthRoute.Login },
            onResend = {
                // TODO: Resend recover password instructions when backend is available.
            }
        )
    }
}

internal data class PasswordRequirement(
    val text: String,
    val isValid: Boolean
)

private val CreateAccountSampleRegisteredEmail = "juan.perez@gmail.com"

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
                Spacer(modifier = Modifier.height(20.dp))
                TracksyLinkText(
                    text = "¿Olvidaste tu contraseña?",
                    onClick = onForgotPassword,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(22.dp))
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

@Composable
fun CreateAccountScreen(
    onCreateAccount: (name: String, email: String, password: String) -> Boolean,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailBlurred by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var passwordBlurred by remember { mutableStateOf(false) }
    var confirmInteracted by remember { mutableStateOf(false) }
    var submitAttempted by remember { mutableStateOf(false) }
    var isEmailAlreadyRegistered by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val passwordRequirements = passwordRequirements(password)
    val isPasswordValid = passwordRequirements.all { it.isValid }
    val isEmailFormatValid = isValidEmail(email)
    val isConfirmValid = confirmPassword.isNotEmpty() && confirmPassword == password
    val isFrontendValid = name.isNotBlank() &&
        isEmailFormatValid &&
        password.isNotEmpty() &&
        isPasswordValid &&
        isConfirmValid
    val emailErrorText = when {
        isEmailAlreadyRegistered -> "Este correo ya está registrado"
        email.isNotBlank() && !isEmailFormatValid && (emailBlurred || submitAttempted) ->
            "Ingresá un correo electrónico válido"
        else -> null
    }
    val showConfirmMismatch = confirmPassword.isNotEmpty() &&
        confirmPassword != password &&
        (confirmInteracted || submitAttempted)

    CreateAccountContent(
        name = name,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        emailErrorText = emailErrorText,
        showPasswordRequirements = passwordFocused || password.isNotEmpty(),
        passwordRequirements = passwordRequirements,
        showPasswordRequirementErrors = passwordBlurred || submitAttempted,
        showConfirmMismatch = showConfirmMismatch,
        isCreateEnabled = isFrontendValid && !isEmailAlreadyRegistered,
        onNameChange = { name = it },
        onEmailChange = {
            email = it
            emailBlurred = false
            isEmailAlreadyRegistered = false
        },
        onEmailFocusChanged = { isFocused ->
            if (!isFocused && email.isNotBlank()) {
                emailBlurred = true
            }
        },
        onPasswordChange = {
            password = it
            if (confirmPassword.isNotEmpty()) {
                confirmInteracted = true
            }
        },
        onPasswordFocusChanged = { isFocused ->
            passwordFocused = isFocused
            if (!isFocused && password.isNotEmpty()) {
                passwordBlurred = true
            }
        },
        onConfirmPasswordChange = {
            confirmPassword = it
            confirmInteracted = true
        },
        onSubmit = {
            submitAttempted = true
            focusManager.clearFocus()
            if (isFrontendValid) {
                isEmailAlreadyRegistered = !onCreateAccount(name, email, password)
            }
        },
        onLogin = onLogin,
        modifier = modifier
    )
}

@Composable
internal fun CreateAccountContent(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    emailErrorText: String?,
    showPasswordRequirements: Boolean,
    passwordRequirements: List<PasswordRequirement>,
    showPasswordRequirementErrors: Boolean,
    showConfirmMismatch: Boolean,
    isCreateEnabled: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onEmailFocusChanged: (Boolean) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordFocusChanged: (Boolean) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    AuthScreenContainer(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 56.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthHeader(title = "Crear cuenta")
                Spacer(modifier = Modifier.height(34.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 254.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TracksyTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = "Nombre"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TracksyTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = "Correo electrónico",
                        isError = emailErrorText != null,
                        onFocusChanged = onEmailFocusChanged,
                        keyboardType = KeyboardType.Email
                    )
                    if (emailErrorText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ErrorMessage(text = emailErrorText)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TracksyPasswordField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = "Contraseña",
                        onFocusChanged = onPasswordFocusChanged,
                        imeAction = ImeAction.Next
                    )
                    if (showPasswordRequirements) {
                        Spacer(modifier = Modifier.height(10.dp))
                        PasswordRequirementChecklist(
                            requirements = passwordRequirements,
                            showErrors = showPasswordRequirementErrors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TracksyPasswordField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = "Confirmá tu contraseña",
                        isError = showConfirmMismatch,
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                    if (showConfirmMismatch) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ErrorMessage(text = "Las contraseñas no coinciden")
                    }
                    Spacer(modifier = Modifier.height(36.dp))
                    TracksyPrimaryButton(
                        text = "Crear cuenta",
                        onClick = onSubmit,
                        enabled = isCreateEnabled
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TracksyInlineLink(
                        text = "¿Ya tenés cuenta?",
                        linkText = "Iniciá sesión",
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun RecoverPasswordScreen(
    onBack: () -> Unit,
    onSubmit: (email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var hasInteracted by remember { mutableStateOf(false) }
    var hasBlurred by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    val isEmailValid = isValidEmail(email)
    val showError = email.isNotBlank() && !isEmailValid && hasBlurred && !isEmailFocused

    RecoverPasswordContent(
        email = email,
        showEmailError = showError,
        onEmailChange = {
            email = it
            hasInteracted = true
        },
        onEmailFocusChanged = { isFocused ->
            isEmailFocused = isFocused
            if (!isFocused && hasInteracted) {
                hasBlurred = true
            }
        },
        onBack = onBack,
        onSubmit = {
            if (isEmailValid) {
                onSubmit(email)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun RecoverPasswordContent(
    email: String,
    showEmailError: Boolean,
    onEmailChange: (String) -> Unit,
    onEmailFocusChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSubmitEnabled = isValidEmail(email)
    val focusManager = LocalFocusManager.current

    AuthScreenContainer(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 74.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthHeader(title = "Recuperar contraseña")
                Spacer(modifier = Modifier.height(54.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 254.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ingresá tu correo electrónico y te enviaremos instrucciones para restablecer tu contraseña.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        color = TracksyTextSecondary,
                        style = AuthBodyTextStyle,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(38.dp))
                    TracksyTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = "Correo electrónico",
                        isError = showEmailError,
                        onFocusChanged = onEmailFocusChanged,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                    if (showEmailError) {
                        Spacer(modifier = Modifier.height(13.dp))
                        ErrorMessage(text = "Ingresá un correo electrónico válido")
                        Spacer(modifier = Modifier.height(35.dp))
                    } else {
                        Spacer(modifier = Modifier.height(76.dp))
                    }
                    TracksyPrimaryButton(
                        text = "Enviar instrucciones",
                        onClick = onSubmit,
                        enabled = isSubmitEnabled
                    )
                }
            }
            AuthBackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 13.dp)
            )
        }
    }
}

@Composable
fun CheckEmailScreen(
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScreenContainer(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 74.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthHeader(title = "Revisá tu correo")
                Spacer(modifier = Modifier.height(54.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 254.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Si existe una cuenta asociada a ese correo, te enviaremos instrucciones para restablecer tu contraseña.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        color = TracksyTextSecondary,
                        style = AuthBodyTextStyle,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = "¿No lo recibiste? Revisá tu carpeta de spam.",
                        modifier = Modifier.fillMaxWidth(),
                        color = TracksyTextMuted,
                        style = AuthBodyTextStyle.copy(fontSize = 11.sp, lineHeight = 14.sp),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(35.dp))
                    TracksyPrimaryButton(
                        text = "Volver a iniciar sesión",
                        onClick = onBackToLogin
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TracksyLinkText(
                        text = "Reenviar instrucciones",
                        onClick = onResend,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            AuthBackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 13.dp)
            )
        }
    }
}

@Composable
private fun AuthBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            drawLine(
                color = TracksyPrimaryPurple,
                start = Offset(size.width * 0.68f, size.height * 0.12f),
                end = Offset(size.width * 0.30f, size.height * 0.50f),
                strokeWidth = 3.6.dp.toPx(),
                cap = StrokeCap.Square
            )
            drawLine(
                color = TracksyPrimaryPurple,
                start = Offset(size.width * 0.30f, size.height * 0.50f),
                end = Offset(size.width * 0.68f, size.height * 0.88f),
                strokeWidth = 3.6.dp.toPx(),
                cap = StrokeCap.Square
            )
        }
    }
}

@Composable
private fun PasswordRequirementChecklist(
    requirements: List<PasswordRequirement>,
    showErrors: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(start = 17.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        requirements.forEach { requirement ->
            PasswordRequirementRow(
                text = requirement.text,
                isValid = requirement.isValid,
                showError = showErrors && !requirement.isValid
            )
        }
    }
}

@Composable
private fun PasswordRequirementRow(
    text: String,
    isValid: Boolean,
    showError: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        isValid -> TracksySuccessGreen
        showError -> TracksyErrorRed
        else -> TracksyChecklistNeutral
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(11.dp)) {
            when {
                isValid -> {
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.12f, size.height * 0.55f),
                        end = Offset(size.width * 0.40f, size.height * 0.82f),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.40f, size.height * 0.82f),
                        end = Offset(size.width * 0.88f, size.height * 0.18f),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                showError -> {
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.18f, size.height * 0.18f),
                        end = Offset(size.width * 0.82f, size.height * 0.82f),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.82f, size.height * 0.18f),
                        end = Offset(size.width * 0.18f, size.height * 0.82f),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                else -> {
                    drawCircle(
                        color = color,
                        radius = size.minDimension * 0.34f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4.dp.toPx())
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = TracksyPrimaryPurple,
            style = PasswordRequirementTextStyle
        )
    }
}

internal fun passwordRequirements(password: String): List<PasswordRequirement> {
    return listOf(
        PasswordRequirement("8+ caracteres", password.length >= 8),
        PasswordRequirement("Carácter especial", password.any { !it.isLetterOrDigit() }),
        PasswordRequirement("Una mayúscula", password.any { it.isUpperCase() }),
        PasswordRequirement("Una minúscula", password.any { it.isLowerCase() }),
        PasswordRequirement("Un número", password.any { it.isDigit() })
    )
}

private fun isValidEmail(email: String): Boolean {
    return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
}
