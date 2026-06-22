package com.example.tracksy.ui.profile

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    var nombre by remember(usuario.nombre) { mutableStateOf(usuario.nombre) }
    var fotoUri by remember(usuario.fotoUri) { mutableStateOf(usuario.fotoUri) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            fotoUri = uri.toString()
        }
    }

    val hasChanges = nombre.trim() != usuario.nombre.trim() || fotoUri != usuario.fotoUri
    val canSave = nombre.isNotBlank() && hasChanges

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
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .clickable { photoPicker.launch(arrayOf("image/*")) },
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileAvatarImage(
                                    fotoUri = fotoUri,
                                    colors = colors,
                                    iconSize = 44.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        TextButton(
                            onClick = { photoPicker.launch(arrayOf("image/*")) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Cambiar foto")
                        }

                        Spacer(Modifier.height(16.dp))

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
                            value = usuario.email,
                            onValueChange = {},
                            placeholder = "tu@email.com",
                            enabled = false,
                            colors = colors,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (canSave) {
                            focusManager.clearFocus()
                            onSave(PerfilUsuario(nombre.trim(), usuario.email, fotoUri))
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
    enabled: Boolean = true,
    colors: TracksyColors,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
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
