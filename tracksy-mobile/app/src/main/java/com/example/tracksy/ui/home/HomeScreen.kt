package com.example.tracksy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.components.TracksyPrimaryButtonSmall
import com.example.tracksy.ui.components.TracksySecondaryButtonSmall
import com.example.tracksy.ui.profile.ProfileAvatarImage
import com.example.tracksy.ui.theme.*

data class Suggestion(
    val productoId: Long? = null,   // EAN-13 barcode
    val emoji: String,
    val name: String,
    val reason: String
)

data class ShoppingList(
    val id: Int = 0,
    val name: String,
    val totalProducts: Int,
    val pendingProducts: Int
)

enum class NavTab { HOME, LISTS, SCANNER, PRODUCTS, HISTORY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedTab: NavTab,
    onTabChange: (NavTab) -> Unit,
    listas: List<ShoppingList> = emptyList(),
    sugerencias: List<Suggestion> = emptyList(),
    userName: String = "",
    profilePhotoUri: String = "",
    onListTap: (ShoppingList) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAgregarSugerencia: (Suggestion) -> Unit = {},
    onDismissSugerencia: (Suggestion) -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val colors = LocalTracksyColors.current
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            TracksyBottomBar(selected = selectedTab, onSelect = onTabChange)
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullRefreshState,
            modifier = Modifier.padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            TopBar(
                userName = userName,
                profilePhotoUri = profilePhotoUri,
                onProfileClick = onProfileClick,
                colors = colors
            )

            Spacer(Modifier.height(24.dp))

            if (sugerencias.isNotEmpty()) {
                SectionTitle("Sugerencias para vos", colors)
                Spacer(Modifier.height(12.dp))
                sugerencias.forEach { suggestion ->
                    SuggestionCard(
                        suggestion = suggestion,
                        colors = colors,
                        onAgregar = { onAgregarSugerencia(suggestion) },
                        onDismiss = { onDismissSugerencia(suggestion) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(8.dp))
            }

            SectionTitle("Mis listas", colors)
            Spacer(Modifier.height(12.dp))
            if (listas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tenés listas aún. Creá una desde Mis Listas.",
                        color = colors.subtitleText,
                        fontSize = 13.sp
                    )
                }
            } else {
                listas.forEach { list ->
                    ListCard(list = list, colors = colors, onClick = { onListTap(list) })
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
        }
        } // PullToRefreshBox
    }
}

@Composable
private fun TopBar(
    userName: String,
    profilePhotoUri: String,
    onProfileClick: () -> Unit,
    colors: TracksyColors
) {
    val greetingName = userName.trim().ifBlank { "Usuario" }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hola, $greetingName",
            fontSize = 26.sp,
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
            ProfileAvatarImage(
                fotoUri = profilePhotoUri,
                colors = colors,
                iconSize = 24.dp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, colors: TracksyColors) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.sectionText
    )
}

@Composable
private fun SuggestionCard(
    suggestion: Suggestion,
    colors: TracksyColors,
    onAgregar: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.background)
            ) {
                Text(text = suggestion.emoji, fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.titleText
                )
                Text(
                    text = suggestion.reason,
                    fontSize = 12.sp,
                    color = colors.subtitleText
                )
            }

            Spacer(Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TracksyPrimaryButtonSmall(
                    text = "+ Agregar",
                    onClick = onAgregar,
                    modifier = Modifier
                )
                TracksySecondaryButtonSmall(
                    text = "No",
                    onClick = onDismiss,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun ListCard(list: ShoppingList, colors: TracksyColors, onClick: () -> Unit = {}) {
    val pendingLabel = if (list.pendingProducts == list.totalProducts)
        "todos pendientes"
    else
        "${list.pendingProducts} pendientes"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = list.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.titleText
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${list.totalProducts} productos · $pendingLabel",
                fontSize = 13.sp,
                color = colors.subtitleText
            )
        }
    }
}

@Composable
fun TracksyBottomBar(selected: NavTab, onSelect: (NavTab) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = TracksySurfaceCard,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(icon = Icons.Outlined.Home, label = "Inicio", selected = selected == NavTab.HOME, onClick = { onSelect(NavTab.HOME) })
                NavItem(icon = Icons.Outlined.Menu, label = "Listas", selected = selected == NavTab.LISTS, onClick = { onSelect(NavTab.LISTS) })
                Spacer(Modifier.size(64.dp))
                NavItem(icon = Icons.Outlined.Star, label = "Productos", selected = selected == NavTab.PRODUCTS, onClick = { onSelect(NavTab.PRODUCTS) })
                NavItem(icon = Icons.Outlined.History, label = "Historial", selected = selected == NavTab.HISTORY, onClick = { onSelect(NavTab.HISTORY) })
            }
        }
        QrNavButton(
            onClick = { onSelect(NavTab.SCANNER) },
            modifier = Modifier.align(Alignment.TopCenter).offset(x = -10.dp)
        )
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) TracksyPrimaryPurple else TracksyTextMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 20.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(text = label, fontSize = 10.sp, color = tint, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
fun QrNavButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(TracksyDisabledButtonBackground)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(TracksyPrimaryPurple)
        ) {
            Icon(
                imageVector = Icons.Outlined.QrCodeScanner,
                contentDescription = "Escanear código",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
