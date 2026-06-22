package com.example.tracksy.ui.profile

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
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
import com.example.tracksy.ui.components.TracksyPrimaryButton
import com.example.tracksy.ui.components.TracksyTextAction
import com.example.tracksy.ui.components.TracksyTextField
import com.example.tracksy.ui.theme.LocalTracksyColors

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
                        TracksyTextAction(
                            text = "Cambiar foto",
                            onClick = { photoPicker.launch(arrayOf("image/*")) },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            icon = Icons.Outlined.PhotoCamera
                        )

                        Spacer(Modifier.height(16.dp))

                        TracksyTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = "Nombre",
                            imeAction = ImeAction.Next,
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        TracksyTextField(
                            value = usuario.email,
                            onValueChange = {},
                            label = "Correo electrónico",
                            enabled = false,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                TracksyPrimaryButton(
                    text = "Guardar cambios",
                    onClick = {
                        if (canSave) {
                            focusManager.clearFocus()
                            onSave(PerfilUsuario(nombre.trim(), usuario.email, fotoUri))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSave
                )

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}
