package com.example.tracksy.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.auth.TracksyPrimaryButton
import com.example.tracksy.ui.theme.TracksyBackgroundLight
import com.example.tracksy.ui.theme.TracksyPrimaryPurple

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onItemClick: (HistoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TracksyTopBar(
                title = "Historial",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            TracksyBottomNavigation(selectedItem = "Historial")
        },
        containerColor = TracksyBackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(HistoryMockData) { item ->
                HistoryCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
fun HistoryDetailScreen(
    item: HistoryItem,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TracksyTopBar(
                title = item.supermarketName,
                onBackClick = onBackClick
            )
        },
        containerColor = TracksyBackgroundLight
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Bloque resumen destacado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TracksyPrimaryPurple)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                val statusText = if (item.status == PurchaseStatus.COMPLETED) "Completada" else "Incompleta"
                Text(
                    text = "${item.dateLabel} · ${item.productCount} productos · $statusText",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.totalAmount,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Card con lista de productos
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(item.products) { product ->
                            ProductItemRow(product = product)
                            if (item.products.last() != product) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    thickness = 1.dp,
                                    color = TracksyBackgroundLight
                                )
                            }
                        }
                    }
                }
            }
            
            // Botón inferior
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                TracksyPrimaryButton(
                    text = "Reutilizar lista",
                    onClick = { /* Acción mock */ }
                )
            }
        }
    }
}
