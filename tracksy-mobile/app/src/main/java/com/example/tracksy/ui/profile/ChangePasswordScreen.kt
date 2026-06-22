package com.example.tracksy.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.components.TracksyErrorMessage
import com.example.tracksy.ui.components.TracksyPasswordField
import com.example.tracksy.ui.components.TracksyPrimaryButton
import com.example.tracksy.ui.theme.LocalTracksyColors
import com.example.tracksy.ui.theme.TracksyColors
import kotlinx.coroutines.launch

private enum class CambiarContrasenaStep { NuevaContrasena, Exito }

@Composable
fun CambiarContrasenaScreen(
    onChangePassword: suspend (passwordActual: String, passwordNuevo: String) -> String?,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var step by remember { mutableStateOf(CambiarContrasenaStep.NuevaContrasena) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    when (step) {
        CambiarContrasenaStep.NuevaContrasena -> NuevaContrasenaStep(
            isLoading = isLoading,
            errorText = errorText,
            onBack = onBack,
            onSubmit = { passwordActual, passwordNuevo ->
                errorText = null
                scope.launch {
                    isLoading = true
                    val error = onChangePassword(passwordActual, passwordNuevo)
                    isLoading = false
                    if (error == null) {
                        step = CambiarContrasenaStep.Exito
                    } else {
                        errorText = error
                    }
                }
            }
        )
        CambiarContrasenaStep.Exito -> CambioContrasenaExitoStep(
            onBack = { step = CambiarContrasenaStep.NuevaContrasena },
            onDone = onSuccess
        )
    }
}

@Composable
private fun NuevaContrasenaStep(
    isLoading: Boolean,
    errorText: String?,
    onBack: () -> Unit,
    onSubmit: (passwordActual: String, passwordNuevo: String) -> Unit
) {
    val colors = LocalTracksyColors.current
    var contrasenaActual by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var pwFocused by remember { mutableStateOf(false) }
    var pwBlurred by remember { mutableStateOf(false) }
    var confirmInteracted by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val reqs = requisitosContrasena(nuevaContrasena)
    val isPwValid = reqs.all { it.cumplido }
    val isConfirmValid = confirmarContrasena.isNotEmpty() && confirmarContrasena == nuevaContrasena
    val showMismatch = confirmarContrasena.isNotEmpty() &&
        confirmarContrasena != nuevaContrasena && (confirmInteracted || submitted)
    val canSubmit = contrasenaActual.isNotBlank() && isPwValid && isConfirmValid && !isLoading

    Scaffold(containerColor = colors.background) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                ContrasenaTopBar("Cambiar contraseña", onBack, colors)
                Spacer(Modifier.height(20.dp))

                Text(
                    "Ingresá tu contraseña actual y elegí una nueva contraseña.",
                    fontSize = 13.sp,
                    color = colors.subtitleText,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(24.dp))

                PerfilCard(colors) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TracksyPasswordField(
                            value = contrasenaActual,
                            onValueChange = { contrasenaActual = it },
                            label = "Contraseña actual",
                            imeAction = ImeAction.Next
                        )

                        Spacer(Modifier.height(16.dp))

                        TracksyPasswordField(
                            value = nuevaContrasena,
                            label = "Nueva contraseña",
                            onValueChange = {
                                nuevaContrasena = it
                                if (confirmarContrasena.isNotEmpty()) confirmInteracted = true
                            },
                            imeAction = ImeAction.Next,
                            onFocusChanged = { focused ->
                                pwFocused = focused
                                if (!focused && nuevaContrasena.isNotEmpty()) pwBlurred = true
                            }
                        )
                        if (pwFocused || nuevaContrasena.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            RequisitosContrasena(reqs, pwBlurred || submitted, colors)
                        }

                        Spacer(Modifier.height(16.dp))

                        TracksyPasswordField(
                            value = confirmarContrasena,
                            label = "Confirmá tu contraseña",
                            onValueChange = { confirmarContrasena = it; confirmInteracted = true },
                            isError = showMismatch,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                        if (showMismatch) {
                            Spacer(Modifier.height(10.dp))
                            TracksyErrorMessage("Las contraseñas no coinciden")
                        }

                        if (errorText != null) {
                            Spacer(Modifier.height(10.dp))
                            TracksyErrorMessage(errorText)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                TracksyPrimaryButton(
                    text = if (isLoading) "Guardando..." else "Cambiar contraseña",
                    onClick = {
                        submitted = true
                        if (canSubmit) {
                            focusManager.clearFocus()
                            onSubmit(contrasenaActual, nuevaContrasena)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit
                )

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun CambioContrasenaExitoStep(
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val colors = LocalTracksyColors.current

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            ContrasenaTopBar("Cambiar contraseña", onBack, colors)
            Spacer(Modifier.height(56.dp))

            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Contraseña actualizada",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.titleText
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "La contraseña de tu cuenta fue actualizada correctamente.",
                fontSize = 14.sp,
                color = colors.subtitleText,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "La próxima vez que inicies sesión, usá la nueva contraseña.",
                fontSize = 13.sp,
                color = colors.subtitleText,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(8.dp))

            Spacer(Modifier.height(40.dp))

            TracksyPrimaryButton(
                text = "Volver al perfil",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ContrasenaTopBar(title: String, onBack: () -> Unit, colors: TracksyColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Volver",
            tint = colors.titleText,
            modifier = Modifier.size(24.dp).clickable(onClick = onBack)
        )
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.titleText)
        Spacer(Modifier.size(24.dp))
    }
}

private data class RequisitoContrasena(val texto: String, val cumplido: Boolean)

private fun requisitosContrasena(password: String): List<RequisitoContrasena> = listOf(
    RequisitoContrasena("8+ caracteres", password.length >= 8),
    RequisitoContrasena("Carácter especial", password.any { !it.isLetterOrDigit() }),
    RequisitoContrasena("Una mayúscula", password.any { it.isUpperCase() }),
    RequisitoContrasena("Una minúscula", password.any { it.isLowerCase() }),
    RequisitoContrasena("Un número", password.any { it.isDigit() })
)

@Composable
private fun RequisitosContrasena(
    reqs: List<RequisitoContrasena>,
    mostrarErrores: Boolean,
    colors: TracksyColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        reqs.forEach { req ->
            val color = when {
                req.cumplido -> colors.successGreen
                mostrarErrores -> colors.errorRed
                else -> colors.subtitleText
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (req.cumplido) "✓" else "·",
                    fontSize = 14.sp,
                    color = color,
                    fontWeight = if (req.cumplido) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(Modifier.width(6.dp))
                Text(text = req.texto, fontSize = 11.sp, color = color)
            }
        }
    }
}
