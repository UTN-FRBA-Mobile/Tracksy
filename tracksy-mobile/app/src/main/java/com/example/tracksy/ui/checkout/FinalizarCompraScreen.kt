package com.example.tracksy.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.components.TracksyPrimaryButton
import com.example.tracksy.ui.components.TracksySecondaryButton
import com.example.tracksy.ui.components.TracksyTextAction
import com.example.tracksy.ui.components.TracksyTextField
import com.example.tracksy.ui.theme.LocalTracksyColors
import com.example.tracksy.ui.theme.TracksyColors
import com.example.tracksy.ui.theme.TracksySuccessGreen
import com.example.tracksy.ui.theme.TracksyWarningBadgeBackground

data class PurchaseSummary(
    val listName: String,
    val purchasedProducts: Int,
    val totalProducts: Int,
    val totalSpent: Int,
    val pendingItems: List<String>
) {
    val pendingCount: Int get() = pendingItems.size
    val hasPending: Boolean get() = pendingItems.isNotEmpty()
}

@Composable
fun FinalizarCompraScreen(
    summary: PurchaseSummary,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onCrearListaPendientes: (nombre: String) -> Unit = {}
) {
    val colors = LocalTracksyColors.current
    var showNombreDialog by remember { mutableStateOf(false) }
    var nombreLista by remember { mutableStateOf("") }
    var listaYaCreada by remember { mutableStateOf(false) }
    var listaOmitida by remember { mutableStateOf(false) }

    if (showNombreDialog) {
        AlertDialog(
            onDismissRequest = { showNombreDialog = false; nombreLista = "" },
            containerColor    = colors.surface,
            titleContentColor = colors.titleText,
            textContentColor  = colors.subtitleText,
            title = { Text("Nombre de la nueva lista") },
            text = {
                TracksyTextField(
                    value = nombreLista,
                    onValueChange = { nombreLista = it },
                    label = "Ingresá un nombre..."
                )
            },
            confirmButton = {
                TracksyTextAction(
                    text = "Crear",
                    onClick = {
                        if (nombreLista.isNotBlank()) {
                            onCrearListaPendientes(nombreLista.trim())
                            listaYaCreada = true
                            showNombreDialog = false
                            nombreLista = ""
                        }
                    },
                    enabled = nombreLista.isNotBlank()
                )
            },
            dismissButton = {
                TracksyTextAction(
                    text = "Cancelar",
                    onClick = { showNombreDialog = false; nombreLista = "" },
                    contentColor = colors.subtitleText
                )
            }
        )
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
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 96.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                FinalizarCompraTopBar(onBack = onBack, colors = colors)

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = colors.titleText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Resumen de compra",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.titleText
                    )
                }

                Spacer(Modifier.height(20.dp))

                SummaryCard(summary = summary, colors = colors)

                if (summary.hasPending && !listaOmitida) {
                    Spacer(Modifier.height(16.dp))
                    PendingListSuggestion(
                        pendingItems  = summary.pendingItems,
                        listaYaCreada = listaYaCreada,
                        colors        = colors,
                        onCrearLista  = { showNombreDialog = true },
                        onOmitir      = { listaOmitida = true }
                    )
                }
            }

            TracksyPrimaryButton(
                text = "Confirmar y guardar en historial",
                onClick = onConfirm,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun FinalizarCompraTopBar(onBack: () -> Unit, colors: TracksyColors) {
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
        Text(text = "Finalizar compra", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.titleText)
        Spacer(Modifier.size(42.dp))
    }
}

@Composable
private fun SummaryCard(summary: PurchaseSummary, colors: TracksyColors) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            SummaryRow(label = "Productos comprados", value = "${summary.purchasedProducts} de ${summary.totalProducts}", colors = colors)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.divider)
            SummaryRow(
                label = "Total gastado",
                value = "$" + "%,d".format(summary.totalSpent).replace(',', '.'),
                emphasized = true,
                colors = colors
            )
            if (summary.hasPending) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.divider)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sin comprar", fontSize = 15.sp, color = colors.titleText)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(TracksyWarningBadgeBackground)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${summary.pendingCount} pendientes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.titleText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasized: Boolean = false, colors: TracksyColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, color = colors.titleText)
        Text(
            text = value,
            fontSize = if (emphasized) 20.sp else 15.sp,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasized) colors.primary else colors.titleText
        )
    }
}

@Composable
private fun PendingListSuggestion(
    pendingItems: List<String>,
    listaYaCreada: Boolean,
    colors: TracksyColors,
    onCrearLista: () -> Unit,
    onOmitir: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                text = "¿Agregar pendientes a una nueva lista?",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.titleText
            )
            Spacer(Modifier.height(6.dp))
            Text(text = pendingItems.joinToString(", "), fontSize = 13.sp, color = colors.subtitleText)
            Spacer(Modifier.height(14.dp))
            if (listaYaCreada) {
                Text(
                    text = "Lista creada con los productos pendientes",
                    fontSize = 13.sp,
                    color = TracksySuccessGreen,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TracksyPrimaryButton(
                        text = "Sí, crear lista",
                        onClick = onCrearLista,
                        modifier = Modifier.weight(1f)
                    )
                    TracksySecondaryButton(
                        text = "No, omitir",
                        onClick = onOmitir,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
