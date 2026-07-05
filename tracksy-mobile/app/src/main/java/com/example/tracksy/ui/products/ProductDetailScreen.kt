package com.example.tracksy.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
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
import com.example.tracksy.ui.components.TracksyPrimaryButton
import com.example.tracksy.ui.components.TracksyTextAction
import com.example.tracksy.ui.profile.ProfileAvatarImage
import com.example.tracksy.ui.theme.LocalTracksyColors

@Composable
fun ProductDetailScreen(
    product: Product,
    listas: List<ListaCompra>,
    draftSelections: Map<Int, Int>? = null,
    profilePhotoUri: String = "",
    onBack: () -> Unit,
    selectedTab: NavTab,
    onTabChange: (NavTab) -> Unit,
    onConfirmarCambios: (
        productoId: Long,
        agregar: List<Pair<Int, Int>>,
        eliminar: List<Pair<Int, Int>>,
        actualizar: List<Triple<Int, Int, Int>>
    ) -> Unit,
    onNuevaLista: (currentSelections: Map<Int, Int>) -> Unit = {}
) {
    val colors = LocalTracksyColors.current

    // Snapshot de los items originales: listaId -> ItemProducto (null si no estaba)
    val originalItems: Map<Int, ItemProducto?> = remember(listas, product) {
        listas.associate { lista ->
            lista.id to lista.items.firstOrNull { it.producto == product.id }
        }
    }

    // Estado confirmado en el backend: listaId -> cantidad
    val initialSelections: Map<Int, Int> = remember(originalItems) {
        originalItems
            .mapNotNull { (listaId, item) -> item?.let { listaId to it.cantidad } }
            .toMap()
    }

    // Borrador mutable: arranca del draft guardado (si existe) o del estado del backend.
    // No usa initialSelections como key para no resetear cuando lleguen nuevas listas.
    var selectedListQuantities by remember {
        mutableStateOf(draftSelections ?: initialSelections)
    }

    // Cuando listasDetalladas se actualiza (ej.: lista nueva creada), mergea las entradas
    // nuevas del backend sin tocar el borrador existente del usuario.
    LaunchedEffect(initialSelections) {
        val nuevasEntradas = initialSelections.filter { (id, _) -> id !in selectedListQuantities }
        if (nuevasEntradas.isNotEmpty()) {
            selectedListQuantities = selectedListQuantities + nuevasEntradas
        }
    }

    val hasChanges = selectedListQuantities != initialSelections

    var showExitAlert    by remember { mutableStateOf(false) }
    var showConfirmAlert by remember { mutableStateOf(false) }

    val tryBack = { if (hasChanges) showExitAlert = true else onBack() }

    BackHandler { tryBack() }

    fun buildChanges(): Triple<List<Pair<Int, Int>>, List<Pair<Int, Int>>, List<Triple<Int, Int, Int>>> {
        val agregar = selectedListQuantities
            .filter { (listaId, _) -> originalItems[listaId] == null }
            .map { (listaId, cantidad) -> Pair(listaId, cantidad) }

        val eliminar = originalItems.entries
            .filter { (listaId, item) -> item != null && listaId !in selectedListQuantities }
            .mapNotNull { (listaId, item) -> item?.let { Pair(listaId, it.id) } }

        val actualizar = selectedListQuantities.entries
            .mapNotNull { (listaId, cantidad) ->
                val item = originalItems[listaId]
                if (item != null && item.cantidad != cantidad) Triple(listaId, item.id, cantidad) else null
            }

        return Triple(agregar, eliminar, actualizar)
    }

    if (showConfirmAlert) {
        AlertDialog(
            onDismissRequest  = { showConfirmAlert = false },
            containerColor    = colors.surface,
            titleContentColor = colors.titleText,
            textContentColor  = colors.subtitleText,
            title = { Text("¿Confirmar cambios?") },
            text  = { Text("Se aplicarán los cambios en las listas seleccionadas.") },
            confirmButton = {
                TracksyTextAction(
                    text = "Confirmar",
                    onClick = {
                        showConfirmAlert = false
                        val (agregar, eliminar, actualizar) = buildChanges()
                        onConfirmarCambios(product.id, agregar, eliminar, actualizar)
                    }
                )
            },
            dismissButton = {
                TracksyTextAction(
                    text = "Cancelar",
                    onClick = { showConfirmAlert = false },
                    contentColor = colors.subtitleText
                )
            }
        )
    }

    if (showExitAlert) {
        AlertDialog(
            onDismissRequest  = { showExitAlert = false },
            containerColor    = colors.surface,
            titleContentColor = colors.titleText,
            textContentColor  = colors.subtitleText,
            title = { Text("¿Salir sin confirmar?") },
            text  = { Text("Tenés cambios sin confirmar. Si salís ahora, se perderán.") },
            confirmButton = {
                TracksyTextAction(
                    text = "Salir",
                    onClick = { showExitAlert = false; onBack() },
                    contentColor = colors.errorRed
                )
            },
            dismissButton = {
                TracksyTextAction(
                    text = "Cancelar",
                    onClick = { showExitAlert = false },
                    contentColor = colors.subtitleText
                )
            }
        )
    }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            TracksyBottomBar(selected = selectedTab, onSelect = onTabChange)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 80.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = colors.titleText,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { tryBack() }
                    )
                    Text(
                        text = "Producto",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.titleText
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.divider)
                    ) {
                        ProfileAvatarImage(
                            fotoUri = profilePhotoUri,
                            colors = colors,
                            iconSize = 24.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Datos del producto
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text       = product.name,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = colors.titleText
                        )
                        if (product.category.isNotBlank() || product.barcode != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = buildString {
                                    append(product.category)
                                    product.barcode?.let { append(" · Cód: $it") }
                                },
                                fontSize = 12.sp,
                                color    = colors.subtitleText
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text       = "Listas",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colors.titleText
                )

                Spacer(Modifier.height(12.dp))

                // Nueva Lista
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNuevaLista(selectedListQuantities) }
                            .padding(horizontal = 18.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text       = "Nueva Lista ...",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color      = colors.titleText,
                            modifier   = Modifier.weight(1f)
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, colors.sectionText, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Nueva lista",
                                tint = colors.sectionText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (listas.isEmpty()) {
                    Text(
                        text     = "No tenés listas creadas todavía.",
                        fontSize = 14.sp,
                        color    = colors.subtitleText,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Listas existentes
                listas.forEach { lista ->
                    val isSelected = lista.id in selectedListQuantities
                    val cantidad   = selectedListQuantities[lista.id] ?: 1

                    Card(
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedListQuantities = if (isSelected) {
                                        selectedListQuantities - lista.id
                                    } else {
                                        // Al seleccionar, arranca con la cantidad que ya tenía
                                        // o 1 si es nueva inclusión
                                        val cantidadInicial = originalItems[lista.id]?.cantidad ?: 1
                                        selectedListQuantities + (lista.id to cantidadInicial)
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text       = lista.nombre,
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = colors.titleText,
                                    modifier   = Modifier.weight(1f)
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected) Modifier.background(colors.primary)
                                            else Modifier.border(1.5.dp, colors.subtitleText, CircleShape)
                                        )
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Botonera de cantidad: solo aparece si la lista está seleccionada
                            if (isSelected) {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text     = "Cantidad:",
                                        fontSize = 13.sp,
                                        color    = colors.subtitleText,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    ListQuantityStepper(
                                        quantity    = cantidad,
                                        colors      = colors,
                                        onDecrement = {
                                            if (cantidad > 1) {
                                                selectedListQuantities = selectedListQuantities + (lista.id to (cantidad - 1))
                                            }
                                        },
                                        onIncrement = {
                                            selectedListQuantities = selectedListQuantities + (lista.id to (cantidad + 1))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Botón confirmar cambios
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                TracksyPrimaryButton(
                    text = "Confirmar cambios",
                    onClick  = { showConfirmAlert = true },
                    enabled  = hasChanges,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ListQuantityStepper(
    quantity: Int,
    colors: com.example.tracksy.ui.theme.TracksyColors,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.background)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(colors.primary)
                .clickable(onClick = onDecrement)
        ) {
            Text("−", color = Color.White, fontSize = 20.sp, lineHeight = 20.sp)
        }

        Text(
            text       = "$quantity",
            fontSize   = 17.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.titleText,
            modifier   = Modifier.padding(horizontal = 14.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(colors.primary)
                .clickable(onClick = onIncrement)
        ) {
            Text("+", color = Color.White, fontSize = 20.sp, lineHeight = 20.sp)
        }
    }
}
