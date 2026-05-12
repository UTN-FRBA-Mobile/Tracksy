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
import androidx.compose.material.icons.filled.Check
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

@Composable
fun DetalleListaScreen(
    onEditar: () -> Unit = {},
    onComparar: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }

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
                    .padding(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "Compra semanal",
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

                Text(
                    "Supermercado:",
                    fontSize = 13.sp,
                    color = TracksySectionText,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TracksySurface)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text("Carrefour", color = TracksySubtitleText, fontSize = 15.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Todos (8)", "Pendientes (3)", "Comprados (5)").forEachIndexed { index, label ->
                        val selected = tabSeleccionado == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) TracksyTitleText else Color.Transparent)
                                .clickable { tabSeleccionado = index }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                label,
                                color = if (selected) Color.White else TracksyTitleText,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProductoDetalle(nombre = "Leche entera x1L", precio = "$1.200", comprado = false)
                    ProductoDetalle(nombre = "Pan lactal", precio = "$950", comprado = false)
                    ProductoDetalle(nombre = "Yogur natural x4u", precio = "$2.100", comprado = false)
                }

                Text(
                    "Comprados",
                    fontSize = 13.sp,
                    color = TracksySectionText,
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProductoDetalle(nombre = "Arroz x1kg", precio = "$800", comprado = true)
                    ProductoDetalle(nombre = "Aceite x1.5L", precio = "$1.650", comprado = true)
                }

                Spacer(Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(TracksyBackground)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onEditar,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = TracksyPrimary)
                    ) {
                        Text("Editar Lista", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    }
                    Button(
                        onClick = onComparar,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = TracksyPrimary)
                    ) {
                        Text("Comparar", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = TracksyTitleText)
                ) {
                    Text("Finalizar Compra", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ProductoDetalle(nombre: String, precio: String, comprado: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TracksySurface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (comprado) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(TracksyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(1.5.dp, TracksySubtitleText, CircleShape)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = nombre,
            modifier = Modifier.weight(1f),
            color = if (comprado) TracksySubtitleText else TracksyTitleText,
            fontSize = 15.sp
        )
        Text(
            text = precio,
            color = if (comprado) TracksySubtitleText else TracksyTitleText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
