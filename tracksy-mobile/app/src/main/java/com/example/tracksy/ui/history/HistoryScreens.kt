package com.example.tracksy.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.example.tracksy.screens.NavTab
import com.example.tracksy.screens.TracksyBottomBar
import com.example.tracksy.ui.auth.TracksyPrimaryButton
import com.example.tracksy.ui.theme.*

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onItemClick: (HistoryItem) -> Unit,
    selectedTab: NavTab = NavTab.HISTORY,
    onTabChange: (NavTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = TracksyBackground,
        bottomBar = {
            TracksyBottomBar(selected = selectedTab, onSelect = onTabChange)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial",
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

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
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
}

@Composable
fun HistoryDetailScreen(
    item: HistoryItem,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(containerColor = TracksyBackground) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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
                        modifier = Modifier.size(24.dp).clickable { onBackClick() }
                    )
                    Text(
                        text = item.supermarketName,
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

                Spacer(Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TracksyTitleText)
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Surface(
                    color = TracksySurface,
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
                                    color = TracksyDivider
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                TracksyPrimaryButton(
                    text = "Reutilizar lista",
                    onClick = {}
                )
            }
        }
    }
}
