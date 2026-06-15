package com.example.tracksy.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.screens.NavTab
import com.example.tracksy.screens.TracksyBottomBar
import com.example.tracksy.ui.theme.LocalTracksyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onItemClick: (HistoryItem) -> Unit,
    items: List<HistoryItem> = HistoryMockData,
    selectedTab: NavTab = NavTab.HISTORY,
    onTabChange: (NavTab) -> Unit = {},
    onProfileClick: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalTracksyColors.current
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            TracksyBottomBar(selected = selectedTab, onSelect = onTabChange)
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullRefreshState,
            modifier = modifier.padding(paddingValues)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    color = colors.titleText
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.divider)
                        .clickable(onClick = onProfileClick)
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = colors.sectionText, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay compras registradas", color = colors.subtitleText, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items) { item ->
                        HistoryCard(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
        } // PullToRefreshBox
    }
}

@Composable
fun HistoryDetailScreen(
    item: HistoryItem,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTracksyColors.current

    Scaffold(containerColor = colors.background) { paddingValues ->
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
                        tint = colors.titleText,
                        modifier = Modifier.size(24.dp).clickable { onBackClick() }
                    )
                    Text(
                        text = item.listName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.titleText
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(42.dp).clip(CircleShape).background(colors.divider)
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = colors.sectionText, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.titleText)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "${item.dateLabel} · ${item.productCount} productos",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = item.totalAmount, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Surface(
                    color = colors.surface,
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
                                    color = colors.divider
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
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.titleText),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(text = "Reutilizar Lista", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}
