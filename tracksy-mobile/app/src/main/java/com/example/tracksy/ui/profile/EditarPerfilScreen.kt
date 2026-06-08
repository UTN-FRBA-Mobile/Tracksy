package com.example.tracksy.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.LocalTracksyColors
import com.example.tracksy.ui.theme.TracksyColors

@Composable
fun EditarPerfilScreen(
    usuario: PerfilUsuario,
    onSave: (PerfilUsuario) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalTracksyColors.current
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var email by remember { mutableStateOf(usuario.email) }
    var emailBlurred by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isEmailValid = validarEmail(email)
    val hasChanges = nombre.trim() != usuario.nombre || email.trim() != usuario.email
    val canSave = nombre.isNotBlank() && isEmailValid && hasChanges

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
                    Text(
                        "Editar perfil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.titleText
                    )
                    Spacer(Modifier.size(24.dp))
                }

                Spacer(Modifier.height(32.dp))

                PerfilCard(colors) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        PerfilFieldLabel("Nombre", colors)
                        Spacer(Modifier.height(6.dp))
                        PerfilTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            placeholder = "Tu nombre",
                            colors = colors,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        PerfilFieldLabel("Correo electrónico", colors)
                        Spacer(Modifier.height(6.dp))
                        PerfilTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailBlurred = false
                            },
                            placeholder = "tu@email.com",
                            isError = email.isNotBlank() && !isEmailValid && emailBlurred,
                            colors = colors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                emailBlurred = true
                                focusManager.clearFocus()
                            })
                        )
                        if (email.isNotBlank() && !isEmailValid && emailBlurred) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Ingresá un correo electrónico válido",
                                fontSize = 11.sp,
                                color = colors.errorRed
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        emailBlurred = true
                        if (canSave) {
                            focusManager.clearFocus()
                            onSave(PerfilUsuario(nombre.trim(), email.trim()))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = canSave,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Guardar cambios", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
internal fun PerfilFieldLabel(text: String, colors: TracksyColors) {
    Text(text = text, fontSize = 13.sp, color = colors.sectionText, fontWeight = FontWeight.SemiBold)
}

@Composable
internal fun PerfilTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    colors: TracksyColors,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = colors.subtitleText, fontSize = 15.sp) },
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.background,
            unfocusedContainerColor = colors.background,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.divider,
            errorBorderColor = colors.errorRed,
            focusedTextColor = colors.titleText,
            unfocusedTextColor = colors.titleText,
            errorTextColor = colors.titleText,
            cursorColor = colors.primary
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true
    )
}

internal fun validarEmail(email: String): Boolean =
    Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
