package com.example.tracksy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.BackgroundLavender
import com.example.tracksy.ui.theme.CardWhite
import com.example.tracksy.ui.theme.MediumPurple
import com.example.tracksy.ui.theme.PlaceholderGray
import com.example.tracksy.ui.theme.PrimaryDeepPurple
import com.example.tracksy.ui.theme.PurchasedText
import com.example.tracksy.ui.theme.TextPrimary
import com.example.tracksy.ui.theme.TextSecondaryPurple
import com.example.tracksy.ui.theme.TracksyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleListaScreen(
    onEditar: () -> Unit = {},
    onComparar: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compra semanal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLavender)
            )
        },
        containerColor = BackgroundLavender,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLavender)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onEditar,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MediumPurple),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Editar Lista", fontSize = 14.sp)
                    }
                    Button(
                        onClick = onComparar,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MediumPurple),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Comparar", fontSize = 14.sp)
                    }
                }
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepPurple),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Finalizar Compra", fontSize = 14.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                "Supermercado:",
                fontSize = 13.sp,
                color = TextSecondaryPurple,
                fontWeight = FontWeight.Medium
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardWhite)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text("Carrefour", color = PlaceholderGray, fontSize = 15.sp)
            }

            // Custom tab row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Todos (8)", "Pendientes (3)", "Comprados (5)").forEachIndexed { index, label ->
                    val selected = tabSeleccionado == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) PrimaryDeepPurple else Color.Transparent)
                            .clickable { tabSeleccionado = index }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            color = if (selected) Color.White else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            // Pending products
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductoDetalle(nombre = "Leche entera x1L", precio = "$1.200", comprado = false)
                ProductoDetalle(nombre = "Pan lactal", precio = "$950", comprado = false)
                ProductoDetalle(nombre = "Yogur natural x4u", precio = "$2.100", comprado = false)
            }

            Text(
                "Comprados",
                fontSize = 13.sp,
                color = TextSecondaryPurple,
                fontWeight = FontWeight.Medium
            )

            // Purchased products
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductoDetalle(nombre = "Arroz x1kg", precio = "$800", comprado = true)
                ProductoDetalle(nombre = "Aceite x1.5L", precio = "$1.650", comprado = true)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProductoDetalle(nombre: String, precio: String, comprado: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (comprado) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(PrimaryDeepPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(1.5.dp, PlaceholderGray, CircleShape)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = nombre,
            modifier = Modifier.weight(1f),
            color = if (comprado) PurchasedText else TextPrimary,
            fontSize = 15.sp
        )
        Text(
            text = precio,
            color = if (comprado) PurchasedText else TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetalleListaPreview() {
    TracksyTheme(dynamicColor = false) {
        DetalleListaScreen()
    }
}
