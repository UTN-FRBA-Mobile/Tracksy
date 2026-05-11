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

private data class ListOption(val name: String, val productCount: Int)

@Composable
fun ProductDetailScreen(
    product: Product,
    onBack: () -> Unit,
    selectedTab: NavTab,
    onTabChange: (NavTab) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }

    val lists = remember {
        listOf(
            ListOption("Compra semanal", 8),
            ListOption("Lista del mes", 14),
            ListOption("Cumpleaños Juan", 6)
        )
    }

    // Mock: Si una lista ya tiene ese prodcuto, se muestra pre-seleccionada
    val initialLists = remember { setOf("Compra semanal") }
    var selectedLists by remember { mutableStateOf(initialLists) }

    // Estado confirmado: refleja lo último que se confirmó
    var confirmedLists    by remember { mutableStateOf(initialLists) }
    var confirmedQuantity by remember { mutableStateOf(1) }

    var showExitAlert    by remember { mutableStateOf(false) }
    var showConfirmAlert by remember { mutableStateOf(false) }

    val hasChanges = selectedLists != confirmedLists || quantity != confirmedQuantity

    val tryBack = {
        if (hasChanges) showExitAlert = true else onBack()
    }

    BackHandler { tryBack() }

    if (showConfirmAlert) {
        AlertDialog(
            onDismissRequest  = { showConfirmAlert = false },
            containerColor    = TracksySurface,
            titleContentColor = TracksyTitleText,
            textContentColor  = TracksySubtitleText,
            title = { Text("¿Confirmar cambios?") },
            text  = { Text("Se aplicarán los cambios realizados en las listas y la cantidad.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmAlert  = false
                    confirmedLists    = selectedLists
                    confirmedQuantity = quantity
                    onBack()
                }) {
                    Text("Confirmar", color = TracksyPrimary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmAlert = false }) {
                    Text("Cancelar", color = TracksySubtitleText)
                }
            }
        )
    }

    if (showExitAlert) {
        AlertDialog(
            onDismissRequest  = { showExitAlert = false },
            containerColor    = TracksySurface,
            titleContentColor = TracksyTitleText,
            textContentColor  = TracksySubtitleText,
            title = { Text("¿Salir sin confirmar?") },
            text  = { Text("Tenés cambios sin confirmar. Si salís ahora, se perderán.") },
            confirmButton = {
                TextButton(onClick = { showExitAlert = false; onBack() }) {
                    Text("Salir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitAlert = false }) {
                    Text("Cancelar", color = TracksySubtitleText)
                }
            }
        )
    }

    Scaffold(
        containerColor = TracksyBackground,
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
                        tint = TracksyTitleText,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { tryBack() }
                    )
                    Text(
                        text = "Producto",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TracksyTitleText
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TracksyDivider)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Perfil",
                            tint = TracksySectionText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Producto
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = TracksySurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = product.name,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TracksyTitleText
                            )
                            if (product.category.isNotBlank() || product.barcode != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text  = buildString {
                                        append(product.category)
                                        product.barcode?.let { append(" · Cód: $it") }
                                    },
                                    fontSize = 12.sp,
                                    color    = TracksySubtitleText
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        QuantityStepper(
                            quantity    = quantity,
                            onDecrement = { if (quantity > 1) quantity-- },
                            onIncrement = { quantity++ }
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text       = "Listas",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TracksyTitleText
                )

                Spacer(Modifier.height(12.dp))

                // Nueva Lista
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = TracksySurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 18.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text       = "Nueva Lista ...",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color      = TracksyTitleText,
                            modifier   = Modifier.weight(1f)
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, TracksySectionText, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Nueva lista",
                                tint = TracksySectionText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Listas existentes
                lists.forEach { list ->
                    val isSelected = list.name in selectedLists
                    Card(
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = TracksySurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLists = if (list.name in selectedLists)
                                        selectedLists - list.name
                                    else
                                        selectedLists + list.name
                                }
                                .padding(horizontal = 18.dp, vertical = 18.dp)
                        ) {
                            Text(
                                text       = list.name,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color      = TracksyTitleText,
                                modifier   = Modifier.weight(1f)
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (isSelected) Modifier.background(TracksyPrimary)
                                        else Modifier.border(1.5.dp, TracksySubtitleText, CircleShape)
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
                    }
                }
            }

            // Boton confirmar cambios
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(TracksyBackground)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = { showConfirmAlert = true },
                    shape  = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = TracksyTitleText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text       = "Confirmar cambios",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color      = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
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
                .clickable(onClick = onDecrement)
        ) {
            Text("−", color = Color.White, fontSize = 20.sp, lineHeight = 20.sp)
        }

        Text(
            text       = "$quantity",
            fontSize   = 17.sp,
            fontWeight = FontWeight.Bold,
            color      = TracksyTitleText,
            modifier   = Modifier.padding(horizontal = 14.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(TracksyPrimary)
                .clickable(onClick = onIncrement)
        ) {
            Text("+", color = Color.White, fontSize = 20.sp, lineHeight = 20.sp)
        }
    }
}
