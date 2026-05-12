package com.example.tracksy.ui.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.*
import com.example.tracksy.ui.utils.dashedBorder

@Composable
fun EditarListaScreen(
    onConfirmar: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(containerColor = TracksyBackground) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = TracksyTitleText,
                        modifier = Modifier.size(24.dp).clickable { onBack() }
                    )
                    Text(
                        text = "Editar lista",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TracksyTitleText
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(42.dp).clip(CircleShape).background(TracksyDivider)
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = TracksySectionText, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                FieldLabel("Nombre de la lista")
                TracksyEditField(placeholder = "Nombre de la lista")

                FieldLabel("Supermercado")
                TracksyEditField(
                    placeholder = "Elegir supermercado",
                    trailingIcon = {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TracksySubtitleText)
                    }
                )

                FieldLabel("Agregar productos")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dashedBorder(1.dp, TracksyDivider, 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TracksySurface)
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TracksySectionText, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Escanear código de barras", color = TracksyTitleText, fontSize = 15.sp)
                }

                FieldLabel("Ingresar manualmente")
                TracksyEditField(placeholder = "Código de barras")

                FieldLabel("Buscar en catálogo")
                TracksyEditField(
                    placeholder = "Buscar en catálogo...",
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TracksySubtitleText)
                    }
                )

                FieldLabel("Productos en la lista")

                ProductoEditable(nombre = "Leche entera x1L", cantidadInicial = 1)
                ProductoEditable(nombre = "Pan lactal", cantidadInicial = 1)
                ProductoEditable(nombre = "Yogur natural", cantidadInicial = 4)

                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(TracksyBackground)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TracksyTitleText)
                ) {
                    Text("Eliminar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onConfirmar,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = TracksyTitleText)
                ) {
                    Text("Confirmar", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TracksySectionText,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun TracksyEditField(
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text(placeholder, color = TracksySubtitleText, fontSize = 15.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = TracksySurface,
            unfocusedContainerColor = TracksySurface,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        trailingIcon = trailingIcon,
        singleLine = true
    )
}

@Composable
private fun ProductoEditable(nombre: String, cantidadInicial: Int) {
    var qty by remember { mutableIntStateOf(cantidadInicial) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TracksySurface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(1.5.dp, TracksySubtitleText, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(nombre, modifier = Modifier.weight(1f), color = TracksyTitleText, fontSize = 15.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(TracksyBackground)
                .padding(4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(TracksyPrimary)
                    .clickable { if (qty > 0) qty-- }
            ) {
                Text("−", color = Color.White, fontSize = 20.sp, lineHeight = 20.sp)
            }
            Text(
                text = "$qty",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TracksyTitleText,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(TracksyPrimary)
                    .clickable { qty++ }
            ) {
                Text("+", color = Color.White, fontSize = 20.sp, lineHeight = 20.sp)
            }
        }
    }
}
