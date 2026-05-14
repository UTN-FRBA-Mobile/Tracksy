package com.example.tracksy.ui.profile

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
import androidx.compose.material.icons.outlined.Description
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.*

data class PerfilUsuario(
    val nombre: String,
    val email: String
)

@Composable
fun PerfilScreen(
    usuario: PerfilUsuario,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var notificaciones by remember { mutableStateOf(true) }
    var alertasSupermercado by remember { mutableStateOf(true) }
    var modoOscuro by remember { mutableStateOf(false) }

    Scaffold(containerColor = TracksyBackground) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            PerfilTopBar(onBack = onBack)

            Spacer(Modifier.height(24.dp))

            PerfilHeader(usuario = usuario)

            Spacer(Modifier.height(28.dp))

            SectionTitle("Cuenta")
            Spacer(Modifier.height(10.dp))
            PerfilCard {
                PerfilNavRow(
                    icon = Icons.Outlined.Person,
                    label = "Editar perfil",
                    onClick = {}
                )
                Divisor()
                PerfilNavRow(
                    icon = Icons.Outlined.Lock,
                    label = "Cambiar contraseña",
                    onClick = {}
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("Preferencias")
            Spacer(Modifier.height(10.dp))
            PerfilCard {
                PerfilToggleRow(
                    icon = Icons.Outlined.Notifications,
                    label = "Notificaciones",
                    description = "Recordatorios de compra y sugerencias",
                    checked = notificaciones,
                    onCheckedChange = { notificaciones = it }
                )
                Divisor()
                PerfilToggleRow(
                    icon = Icons.Outlined.LocationOn,
                    label = "Alertas de supermercado",
                    description = "Avisos al entrar a un comercio cercano",
                    checked = alertasSupermercado,
                    onCheckedChange = { alertasSupermercado = it }
                )
                Divisor()
                PerfilToggleRow(
                    icon = Icons.Outlined.DarkMode,
                    label = "Modo oscuro",
                    description = null,
                    checked = modoOscuro,
                    onCheckedChange = { modoOscuro = it }
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("Sobre Tracksy")
            Spacer(Modifier.height(10.dp))
            PerfilCard {
                PerfilNavRow(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    label = "Ayuda y soporte",
                    onClick = {}
                )
                Divisor()
                PerfilNavRow(
                    icon = Icons.Outlined.Description,
                    label = "Términos y privacidad",
                    onClick = {}
                )
                Divisor()
                PerfilInfoRow(label = "Versión", value = "1.0.0")
            }

            Spacer(Modifier.height(28.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TracksyErrorRed)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Cerrar sesión",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PerfilTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Volver",
            tint = TracksyTitleText,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBack)
        )
        Text(
            text = "Perfil",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TracksyTitleText
        )
        Spacer(Modifier.size(24.dp))
    }
}

@Composable
private fun PerfilHeader(usuario: PerfilUsuario) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(TracksyDivider)
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Avatar",
                tint = TracksySectionText,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = usuario.nombre,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TracksyTitleText
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = usuario.email,
            fontSize = 13.sp,
            color = TracksySubtitleText
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TracksySectionText
    )
}

@Composable
private fun PerfilCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TracksySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun PerfilNavRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TracksyPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = TracksyTitleText,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TracksySubtitleText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PerfilToggleRow(
    icon: ImageVector,
    label: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TracksyPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 15.sp,
                color = TracksyTitleText
            )
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TracksySubtitleText
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TracksyPrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = TracksyDivider,
                uncheckedBorderColor = TracksyDivider
            )
        )
    }
}

@Composable
private fun PerfilInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = TracksyTitleText,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TracksySubtitleText
        )
    }
}

@Composable
private fun Divisor() {
    HorizontalDivider(
        thickness = 1.dp,
        color = TracksyDivider,
        modifier = Modifier.padding(start = 52.dp)
    )
}
