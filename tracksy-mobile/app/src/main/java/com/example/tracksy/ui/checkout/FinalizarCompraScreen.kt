package com.example.tracksy.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.*

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
    onConfirm: (createPendingList: Boolean) -> Unit
) {
    var createPendingList by remember { mutableStateOf(true) }

    Scaffold(containerColor = TracksyBackground) { padding ->
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
                FinalizarCompraTopBar(onBack = onBack)

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = TracksyTitleText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Resumen de compra",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TracksyTitleText
                    )
                }

                Spacer(Modifier.height(20.dp))

                SummaryCard(summary = summary)

                if (summary.hasPending) {
                    Spacer(Modifier.height(16.dp))
                    PendingListSuggestion(
                        pendingItems = summary.pendingItems,
                        createList = createPendingList,
                        onToggle = { createPendingList = it }
                    )
                }
            }

            Button(
                onClick = { onConfirm(createPendingList && summary.hasPending) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = TracksyPrimary),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Confirmar y guardar en historial",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun FinalizarCompraTopBar(onBack: () -> Unit) {
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
            text = "Finalizar compra",
            fontSize = 18.sp,
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
}

@Composable
private fun SummaryCard(summary: PurchaseSummary) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TracksySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            SummaryRow(
                label = "Productos comprados",
                value = "${summary.purchasedProducts} de ${summary.totalProducts}"
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = TracksyDivider
            )
            SummaryRow(
                label = "Total gastado",
                value = "$" + "%,d".format(summary.totalSpent).replace(',', '.'),
                emphasized = true
            )
            if (summary.hasPending) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = TracksyDivider
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sin comprar",
                        fontSize = 15.sp,
                        color = TracksyTitleText
                    )
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
                            color = TracksyTitleText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasized: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, color = TracksyTitleText)
        Text(
            text = value,
            fontSize = if (emphasized) 20.sp else 15.sp,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasized) TracksyPrimary else TracksyTitleText
        )
    }
}

@Composable
private fun PendingListSuggestion(
    pendingItems: List<String>,
    createList: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, TracksyDivider, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                text = "¿Agregar pendientes a una nueva lista?",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TracksyTitleText
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = pendingItems.joinToString(", "),
                fontSize = 13.sp,
                color = TracksySubtitleText
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onToggle(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (createList) TracksyPrimary else TracksyDivider,
                        contentColor = if (createList) Color.White else TracksyTitleText
                    )
                ) {
                    Text(
                        "Sí, crear lista",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = { onToggle(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (!createList) TracksyPrimary else TracksySubtitleText
                    )
                ) {
                    Text(
                        "No, omitir",
                        fontSize = 13.sp,
                        fontWeight = if (!createList) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
