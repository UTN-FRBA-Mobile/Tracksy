package com.example.tracksy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.*

// Clases de datos
data class Suggestion(
    val emoji: String,
    val name: String,
    val reason: String
)

data class ShoppingList(
    val name: String,
    val totalProducts: Int,
    val pendingProducts: Int
)

// Barra de navegación
enum class NavTab { HOME, LISTS, SCANNER, PRODUCTS, HISTORY }

// Pantalla principal
@Composable
fun HomeScreen(
    selectedTab: NavTab,
    onTabChange: (NavTab) -> Unit,
    onListTap: (ShoppingList) -> Unit = {}
) {
    val suggestions = listOf(
        Suggestion("🥛", "Leche entera",  "La comprás cada 2 semanas"),
        Suggestion("🍞", "Pan lactal",    "Se te puede estar terminando")
    )
    val lists = listOf(
        ShoppingList("Compra semanal",  totalProducts = 8,  pendingProducts = 3),
        ShoppingList("Lista del mes",   totalProducts = 14, pendingProducts = 14)
    )

    Scaffold(
        containerColor = TracksyBackground,
        bottomBar = {
            TracksyBottomBar(selected = selectedTab, onSelect = onTabChange)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Top bar
            TopBar()

            Spacer(Modifier.height(24.dp))

            // Sugerencias
            SectionTitle("Sugerencias para vos")
            Spacer(Modifier.height(12.dp))
            suggestions.forEach { suggestion ->
                SuggestionCard(suggestion)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Mis listas
            SectionTitle("Mis listas")
            Spacer(Modifier.height(12.dp))
            lists.forEach { list ->
                ListCard(list, onClick = { onListTap(list) })
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// Top Bar
@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = "Tracksy",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = TracksyTitleText
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(TracksyDivider)
        ) {
            Icon(
                imageVector        = Icons.Outlined.Person,
                contentDescription = "Perfil",
                tint               = TracksySectionText,
                modifier           = Modifier.size(24.dp)
            )
        }
    }
}

// Subtítulos
@Composable
private fun SectionTitle(text: String) {
    Text(
        text       = text,
        fontSize   = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color      = TracksySectionText
    )
}

// Sugerencia
@Composable
private fun SuggestionCard(suggestion: Suggestion) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TracksySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            modifier             = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // Emoji
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(TracksyBackground)
            ) {
                Text(text = suggestion.emoji, fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Nombre y descripción
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = suggestion.name,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TracksyTitleText
                )
                Text(
                    text     = suggestion.reason,
                    fontSize = 12.sp,
                    color    = TracksySubtitleText
                )
            }

            Spacer(Modifier.width(8.dp))

            // Botones
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {},
                    shape   = RoundedCornerShape(50),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = TracksyPrimary,
                        contentColor   = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier       = Modifier.height(36.dp)
                ) {
                    Text(text = "+ Agregar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = {},
                    shape   = RoundedCornerShape(50),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = TracksySubtitleText),
                    border  = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier       = Modifier.height(36.dp)
                ) {
                    Text(text = "No", fontSize = 12.sp)
                }
            }
        }
    }
}

// Listas
@Composable
private fun ListCard(list: ShoppingList, onClick: () -> Unit = {}) {
    val pendingLabel = if (list.pendingProducts == list.totalProducts)
        "todos pendientes"
    else
        "${list.pendingProducts} pendientes"

    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = TracksySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text       = list.name,
                fontSize   = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TracksyTitleText
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = "${list.totalProducts} productos · $pendingLabel",
                fontSize = 13.sp,
                color    = TracksySubtitleText
            )
        }
    }
}

// Navegación entre screens
@Composable
fun TracksyBottomBar(
    selected: NavTab,
    onSelect: (NavTab) -> Unit
) {
    // The outer Box height = Surface height + 32dp (top half of the 64dp QR button).
    // QrNavButton is at TopCenter → fully inside Box bounds → touch events work correctly.
    // Surface is pushed down 32dp so the QR button visually floats above it.
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color           = TracksySurface,
            shadowElevation = 12.dp,
            shape           = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier        = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                NavItem(
                    icon     = Icons.Outlined.Home,
                    label    = "Inicio",
                    selected = selected == NavTab.HOME,
                    onClick  = { onSelect(NavTab.HOME) }
                )
                NavItem(
                    icon     = Icons.Outlined.Menu,
                    label    = "Listas",
                    selected = selected == NavTab.LISTS,
                    onClick  = { onSelect(NavTab.LISTS) }
                )
                Spacer(Modifier.size(64.dp))
                NavItem(
                    icon     = Icons.Outlined.Star,
                    label    = "Productos",
                    selected = selected == NavTab.PRODUCTS,
                    onClick  = { onSelect(NavTab.PRODUCTS) }
                )
                NavItem(
                    icon     = Icons.Outlined.History,
                    label    = "Historial",
                    selected = selected == NavTab.HISTORY,
                    onClick  = { onSelect(NavTab.HISTORY) }
                )
            }
        }
        QrNavButton(
            onClick  = { onSelect(NavTab.SCANNER) },
            modifier = Modifier.align(Alignment.TopCenter).offset(x = -10.dp)
        )
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) TracksyNavActive else TracksyNavInactive
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 20.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = tint,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text      = label,
            fontSize  = 10.sp,
            color     = tint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun QrNavButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(TracksyQrBorder)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(TracksyPrimary)
        ) {
            Icon(
                imageVector        = Icons.Outlined.QrCodeScanner,
                contentDescription = "Escanear código",
                tint               = Color.White,
                modifier           = Modifier.size(30.dp)
            )
        }
    }
}
