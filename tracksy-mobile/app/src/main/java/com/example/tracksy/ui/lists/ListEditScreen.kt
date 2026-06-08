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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
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
import com.example.tracksy.data.models.ListaCompra
import com.example.tracksy.data.models.Supermercado
import com.example.tracksy.screens.Product
import com.example.tracksy.ui.theme.LocalTracksyColors
import com.example.tracksy.ui.theme.TracksyColors
import com.example.tracksy.ui.utils.dashedBorder

data class ItemDeLista(
    val productoId: Int,
    val nombre: String,
    val cantidad: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarListaScreen(
    listaActual: ListaCompra? = null,
    productosDisponibles: List<Product> = emptyList(),
    supermercados: List<Supermercado> = emptyList(),
    onConfirmar: (nombre: String, supermercadoId: Int?, items: List<ItemDeLista>) -> Unit = { _, _, _ -> },
    onBack: () -> Unit = {},
    onScanBarcode: () -> Unit = {},
    scannedBarcode: String? = null
) {
    val colors = LocalTracksyColors.current

    var listaNombre by remember(listaActual) {
        mutableStateOf(listaActual?.nombre ?: "")
    }
    var supermercadoSeleccionado by remember(listaActual, supermercados) {
        mutableStateOf(supermercados.firstOrNull { it.id == listaActual?.supermercado })
    }
    var supermercadoMenuExpanded by remember { mutableStateOf(false) }

    var usarBarcodeManual by remember { mutableStateOf(false) }
    var barcodeManual by remember { mutableStateOf("") }
    var busquedaCatalogo by remember { mutableStateOf("") }

    var itemsEnLista by remember(listaActual) {
        val initial: List<ItemDeLista> = listaActual?.items?.map {
            ItemDeLista(it.producto, it.productoNombre, it.cantidad)
        } ?: emptyList()
        mutableStateOf(initial)
    }

    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode != null) {
            usarBarcodeManual = false
            busquedaCatalogo = scannedBarcode
        }
    }

    val productosFiltrados = remember(busquedaCatalogo, barcodeManual, usarBarcodeManual, productosDisponibles) {
        val query = if (usarBarcodeManual) barcodeManual else busquedaCatalogo
        if (query.isBlank()) emptyList()
        else productosDisponibles.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.barcode?.contains(query, ignoreCase = true) == true
        }.take(5)
    }

    val tituloScreen = if (listaActual == null) "Nueva lista" else "Editar lista"

    Scaffold(containerColor = colors.background) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
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
                        tint = colors.titleText,
                        modifier = Modifier.size(24.dp).clickable { onBack() }
                    )
                    Text(text = tituloScreen, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.titleText)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(42.dp).clip(CircleShape).background(colors.divider)
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = colors.sectionText, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Nombre ────────────────────────────────────────────────────
                FieldLabel("Nombre de la lista", colors)
                OutlinedTextField(
                    value = listaNombre,
                    onValueChange = { listaNombre = it },
                    placeholder = { Text("Nombre de la lista", color = colors.subtitleText, fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = colors.titleText,
                        unfocusedTextColor = colors.titleText
                    ),
                    singleLine = true
                )

                // ── Supermercado ──────────────────────────────────────────────
                FieldLabel("Supermercado", colors)
                ExposedDropdownMenuBox(
                    expanded = supermercadoMenuExpanded,
                    onExpandedChange = { supermercadoMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = supermercadoSeleccionado?.nombre ?: "Sin supermercado",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = colors.titleText,
                            unfocusedTextColor = if (supermercadoSeleccionado != null) colors.titleText else colors.subtitleText
                        ),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = supermercadoMenuExpanded)
                        },
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = supermercadoMenuExpanded,
                        onDismissRequest = { supermercadoMenuExpanded = false },
                        modifier = Modifier.background(colors.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin supermercado", color = colors.subtitleText, fontSize = 15.sp) },
                            onClick = { supermercadoSeleccionado = null; supermercadoMenuExpanded = false }
                        )
                        supermercados.forEach { super_ ->
                            DropdownMenuItem(
                                text = { Text(super_.nombre, color = colors.titleText, fontSize = 15.sp) },
                                onClick = { supermercadoSeleccionado = super_; supermercadoMenuExpanded = false }
                            )
                        }
                    }
                }

                // ── Agregar productos ─────────────────────────────────────────
                FieldLabel("Agregar productos", colors)

                // Toggle: Escanear vs Manual
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val scanSelected = !usarBarcodeManual
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (scanSelected) colors.primary.copy(alpha = 0.12f) else colors.surface)
                            .border(
                                width = if (scanSelected) 1.5.dp else 1.dp,
                                color = if (scanSelected) colors.primary else colors.divider,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { usarBarcodeManual = false }
                            .padding(vertical = 16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = if (scanSelected) colors.primary else colors.sectionText, modifier = Modifier.size(22.dp))
                            Text("Escanear", color = if (scanSelected) colors.primary else colors.sectionText, fontSize = 13.sp, fontWeight = if (scanSelected) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (usarBarcodeManual) colors.primary.copy(alpha = 0.12f) else colors.surface)
                            .border(
                                width = if (usarBarcodeManual) 1.5.dp else 1.dp,
                                color = if (usarBarcodeManual) colors.primary else colors.divider,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { usarBarcodeManual = true }
                            .padding(vertical = 16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = if (usarBarcodeManual) colors.primary else colors.sectionText, modifier = Modifier.size(22.dp))
                            Text("Manual", color = if (usarBarcodeManual) colors.primary else colors.sectionText, fontSize = 13.sp, fontWeight = if (usarBarcodeManual) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }

                if (!usarBarcodeManual) {
                    // Botón scanner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .dashedBorder(1.dp, colors.divider, 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .clickable { onScanBarcode() }
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = colors.sectionText, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Tocar para escanear código de barras", color = colors.titleText, fontSize = 15.sp)
                    }
                } else {
                    // Campo manual
                    OutlinedTextField(
                        value = barcodeManual,
                        onValueChange = { barcodeManual = it },
                        placeholder = { Text("Ingresar código de barras...", color = colors.subtitleText, fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = colors.titleText,
                            unfocusedTextColor = colors.titleText
                        ),
                        trailingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = colors.subtitleText)
                        },
                        singleLine = true
                    )
                }

                // ── Buscar en catálogo ────────────────────────────────────────
                FieldLabel("Buscar en catálogo", colors)
                OutlinedTextField(
                    value = busquedaCatalogo,
                    onValueChange = { busquedaCatalogo = it },
                    placeholder = { Text("Buscar producto...", color = colors.subtitleText, fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = colors.titleText,
                        unfocusedTextColor = colors.titleText
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = colors.subtitleText)
                    },
                    singleLine = true
                )

                // Resultados del catálogo
                if (productosFiltrados.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                    ) {
                        productosFiltrados.forEach { producto ->
                            val yaAgregado = itemsEnLista.any { it.productoId == producto.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !yaAgregado) {
                                        if (!yaAgregado) {
                                            itemsEnLista = itemsEnLista + ItemDeLista(
                                                productoId = producto.id,
                                                nombre = producto.name
                                            )
                                            busquedaCatalogo = ""
                                            barcodeManual = ""
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = producto.name,
                                        color = if (yaAgregado) colors.subtitleText else colors.titleText,
                                        fontSize = 15.sp
                                    )
                                    if (producto.category.isNotBlank()) {
                                        Text(text = producto.category, color = colors.subtitleText, fontSize = 12.sp)
                                    }
                                }
                                if (yaAgregado) {
                                    Text("Agregado", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                        }
                    }
                }

                // ── Items en la lista ─────────────────────────────────────────
                if (itemsEnLista.isNotEmpty()) {
                    FieldLabel("Productos en la lista", colors)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsEnLista.forEach { item ->
                            ItemEnListaEditable(
                                item = item,
                                colors = colors,
                                onCantidadChange = { nuevaCantidad ->
                                    itemsEnLista = itemsEnLista.map {
                                        if (it.productoId == item.productoId) it.copy(cantidad = nuevaCantidad) else it
                                    }
                                },
                                onEliminar = {
                                    itemsEnLista = itemsEnLista.filter { it.productoId != item.productoId }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ── Botones de acción ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.titleText)
                ) {
                    Text("Cancelar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = {
                        if (listaNombre.isNotBlank()) {
                            onConfirmar(listaNombre.trim(), supermercadoSeleccionado?.id, itemsEnLista)
                        }
                    },
                    enabled = listaNombre.isNotBlank(),
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.titleText)
                ) {
                    Text("Confirmar", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ItemEnListaEditable(
    item: ItemDeLista,
    colors: TracksyColors,
    onCantidadChange: (Int) -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(22.dp).border(1.5.dp, colors.subtitleText, CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(item.nombre, modifier = Modifier.weight(1f), color = colors.titleText, fontSize = 15.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clip(RoundedCornerShape(50)).background(colors.background).padding(4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.primary)
                    .clickable { if (item.cantidad > 1) onCantidadChange(item.cantidad - 1) }
            ) {
                Text("−", color = Color.White, fontSize = 18.sp, lineHeight = 18.sp)
            }
            Text(
                text = "${item.cantidad}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.titleText,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.primary)
                    .clickable { onCantidadChange(item.cantidad + 1) }
            ) {
                Text("+", color = Color.White, fontSize = 18.sp, lineHeight = 18.sp)
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = "Quitar",
            tint = colors.subtitleText,
            modifier = Modifier.size(20.dp).clickable { onEliminar() }
        )
    }
}

@Composable
private fun FieldLabel(text: String, colors: TracksyColors) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = colors.sectionText,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}
