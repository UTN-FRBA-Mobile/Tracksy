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
import com.example.tracksy.data.models.ItemProducto
import com.example.tracksy.data.models.ListaCompra
import com.example.tracksy.data.models.ProductoListado
import com.example.tracksy.data.models.Supermercado
import com.example.tracksy.ui.components.TracksyPrimaryButton
import com.example.tracksy.ui.components.TracksySecondaryButton
import com.example.tracksy.ui.theme.LocalTracksyColors
import com.example.tracksy.ui.theme.TracksyColors

@Composable
fun DetalleListaScreen(
    lista: ListaCompra?,
    supermercados: List<Supermercado> = emptyList(),
    listados: List<ProductoListado> = emptyList(),
    onToggleItem: (listaId: Int, itemId: Int, estaComprado: Boolean) -> Unit = { _, _, _ -> },
    onEditar: () -> Unit = {},
    onComparar: () -> Unit = {},
    onFinalizar: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val colors = LocalTracksyColors.current
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    val items = lista?.items ?: emptyList()
    val pendientes = items.filter { !it.esComprado() }
    val comprados  = items.filter { it.esComprado() }

    val preciosPorProducto: Map<Long, Double> = remember(listados, lista?.supermercado) {
        val supId = lista?.supermercado ?: return@remember emptyMap()
        listados.filter { it.supermercado == supId }.associate { it.producto to it.precio }
    }

    val itemsFiltrados = when (tabSeleccionado) {
        1 -> pendientes
        2 -> comprados
        else -> items
    }

    Scaffold(containerColor = colors.background) { padding ->
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
                        tint = colors.titleText,
                        modifier = Modifier.size(24.dp).clickable { onBack() }
                    )
                    Text(
                        text = lista?.nombre ?: "Lista",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.titleText
                    )
                    Spacer(Modifier.size(42.dp))
                }

                Text("Supermercado", fontSize = 13.sp, color = colors.sectionText, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = if (lista?.supermercado != null) {
                        supermercados.firstOrNull { it.id == lista.supermercado }?.nombre
                            ?: "Supermercado #${lista.supermercado}"
                    } else "Sin supermercado",
                        color = colors.subtitleText,
                        fontSize = 15.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "Todos (${items.size})",
                        "Pendientes (${pendientes.size})",
                        "Comprados (${comprados.size})"
                    ).forEachIndexed { index, label ->
                        val selected = tabSeleccionado == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) colors.titleText else Color.Transparent)
                                .clickable { tabSeleccionado = index }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                label,
                                color = if (selected) Color.White else colors.titleText,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (lista == null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                } else if (itemsFiltrados.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = when (tabSeleccionado) {
                                1 -> "Sin productos pendientes"
                                2 -> "Sin productos comprados"
                                else -> "La lista está vacía"
                            },
                            color = colors.subtitleText,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsFiltrados.forEach { item ->
                            ItemProductoRow(
                                item   = item,
                                colors = colors,
                                precio = preciosPorProducto[item.producto],
                                onToggle = {
                                    lista?.let { onToggleItem(it.id, item.id, item.esComprado()) }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TracksySecondaryButton(
                        text = "Editar Lista",
                        onClick = onEditar,
                        modifier = Modifier.weight(1f)
                    )
                    TracksySecondaryButton(
                        text = "Comparar",
                        onClick = onComparar,
                        modifier = Modifier.weight(1f)
                    )
                }
                TracksyPrimaryButton(
                    text = "Finalizar Compra",
                    onClick = onFinalizar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ItemProductoRow(item: ItemProducto, colors: TracksyColors, precio: Double? = null, onToggle: () -> Unit) {
    val comprado = item.esComprado()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (comprado) {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape).background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        } else {
            Box(modifier = Modifier.size(22.dp).border(1.5.dp, colors.subtitleText, CircleShape))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productoNombre,
                color = if (comprado) colors.subtitleText else colors.titleText,
                fontSize = 15.sp
            )
            if (item.cantidad > 1) {
                Text(text = "x${item.cantidad}", color = colors.subtitleText, fontSize = 12.sp)
            }
        }
        val displayPrice = precio ?: if (item.precioUnitario > 0) item.precioUnitario else null
        if (displayPrice != null) {
            Text(
                text = "$%.0f".format(displayPrice * item.cantidad),
                color = colors.subtitleText,
                fontSize = 15.sp
            )
        }
    }
}

private fun ItemProducto.esComprado(): Boolean =
    estadoNombre.lowercase().let { it.contains("comprad") || it.contains("completad") }
