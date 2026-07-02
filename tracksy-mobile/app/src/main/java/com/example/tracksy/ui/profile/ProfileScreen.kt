package com.example.tracksy.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.components.TracksyDestructiveButton
import com.example.tracksy.ui.theme.LocalTracksyColors
import com.example.tracksy.ui.theme.TracksyColors

data class PerfilUsuario(
    val nombre: String,
    val email: String,
    val fotoUri: String = ""
)

@Composable
fun PerfilScreen(
    usuario: PerfilUsuario,
    isDarkMode: Boolean,
    notificacionesEnabled: Boolean,
    onNotificacionesChange: (Boolean) -> Unit,
    alertasSupermercadoEnabled: Boolean,
    onAlertasSupermercadoChange: (Boolean) -> Unit,
    distanciaMetros: Int,
    onDistanciaChange: (Int) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onEditarPerfil: () -> Unit,
    onCambiarContrasena: () -> Unit,
    onModoOscuroChange: (Boolean) -> Unit,
    onAyudaSoporte: () -> Unit = {}
) {
    val colors = LocalTracksyColors.current

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            PerfilTopBar(onBack = onBack, colors = colors)
            Spacer(Modifier.height(24.dp))
            PerfilHeader(usuario = usuario, colors = colors)
            Spacer(Modifier.height(28.dp))

            SectionTitle("Cuenta", colors)
            Spacer(Modifier.height(10.dp))
            PerfilCard(colors) {
                PerfilNavRow(Icons.Outlined.Person, "Editar perfil", onEditarPerfil, colors)
                Divisor(colors)
                PerfilNavRow(Icons.Outlined.Lock, "Cambiar contraseña", onCambiarContrasena, colors)
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("Preferencias", colors)
            Spacer(Modifier.height(10.dp))
            PerfilCard(colors) {
                PerfilToggleRow(
                    icon = Icons.Outlined.Notifications,
                    label = "Notificaciones",
                    description = "Recordatorios de compra y sugerencias",
                    checked = notificacionesEnabled,
                    onCheckedChange = onNotificacionesChange,
                    colors = colors
                )
                Divisor(colors)
                PerfilToggleRow(
                    icon = Icons.Outlined.LocationOn,
                    label = "Alertas de supermercado",
                    description = "Avisos al entrar a un comercio cercano",
                    checked = alertasSupermercadoEnabled,
                    onCheckedChange = onAlertasSupermercadoChange,
                    colors = colors
                )
                if (alertasSupermercadoEnabled) {
                    Divisor(colors)
                    DistanciaSliderRow(
                        distanciaMetros = distanciaMetros,
                        onDistanciaChange = onDistanciaChange,
                        colors = colors
                    )
                }
                Divisor(colors)
                PerfilToggleRow(
                    icon = Icons.Outlined.DarkMode,
                    label = "Modo oscuro",
                    description = null,
                    checked = isDarkMode,
                    onCheckedChange = onModoOscuroChange,
                    colors = colors
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("Sobre Tracksy", colors)
            Spacer(Modifier.height(10.dp))
            PerfilCard(colors) {
                PerfilNavRow(Icons.AutoMirrored.Outlined.HelpOutline, "Ayuda y soporte", onAyudaSoporte, colors)
                Divisor(colors)
                PerfilInfoRow("Versión", "1.0.0", colors)
            }

            Spacer(Modifier.height(28.dp))

            TracksyDestructiveButton(
                text = "Cerrar sesión",
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Outlined.Logout
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PerfilTopBar(onBack: () -> Unit, colors: TracksyColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Volver",
            tint = colors.titleText,
            modifier = Modifier.size(24.dp).clickable(onClick = onBack)
        )
        Text("Perfil", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.titleText)
        Spacer(Modifier.size(24.dp))
    }
}

@Composable
private fun PerfilHeader(usuario: PerfilUsuario, colors: TracksyColors) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(84.dp).clip(CircleShape).background(colors.divider)
        ) {
            ProfileAvatarImage(
                fotoUri = usuario.fotoUri,
                colors = colors,
                iconSize = 44.dp,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(text = usuario.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.titleText)
        Spacer(Modifier.height(4.dp))
        Text(text = usuario.email, fontSize = 13.sp, color = colors.subtitleText)
    }
}

@Composable
internal fun ProfileAvatarImage(
    fotoUri: String,
    colors: TracksyColors,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageBitmap by produceState<ImageBitmap?>(null, fotoUri) {
        value = if (fotoUri.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(fotoUri)).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Foto de perfil",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "Avatar",
            tint = colors.sectionText,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
internal fun SectionTitle(text: String, colors: TracksyColors) {
    Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.sectionText)
}

@Composable
internal fun PerfilCard(colors: TracksyColors, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun PerfilNavRow(icon: ImageVector, label: String, onClick: () -> Unit, colors: TracksyColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(text = label, fontSize = 15.sp, color = colors.titleText, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.subtitleText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun PerfilToggleRow(
    icon: ImageVector,
    label: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: TracksyColors
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 15.sp, color = colors.titleText)
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(text = description, fontSize = 12.sp, color = colors.subtitleText)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.divider,
                uncheckedBorderColor = colors.divider
            )
        )
    }
}

@Composable
private fun PerfilInfoRow(label: String, value: String, colors: TracksyColors) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, color = colors.titleText, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 14.sp, color = colors.subtitleText)
    }
}

@Composable
internal fun Divisor(colors: TracksyColors) {
    HorizontalDivider(thickness = 1.dp, color = colors.divider, modifier = Modifier.padding(start = 52.dp))
}

@Composable
private fun DistanciaSliderRow(
    distanciaMetros: Int,
    onDistanciaChange: (Int) -> Unit,
    colors: TracksyColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Distancia de alerta", fontSize = 15.sp, color = colors.titleText)
                Text("$distanciaMetros metros", fontSize = 12.sp, color = colors.subtitleText)
            }
        }
        Slider(
            value = distanciaMetros.toFloat(),
            onValueChange = { onDistanciaChange(it.toInt()) },
            valueRange = 100f..1000f,
            steps = 8,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 36.dp, top = 4.dp),
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary
            )
        )
    }
}
