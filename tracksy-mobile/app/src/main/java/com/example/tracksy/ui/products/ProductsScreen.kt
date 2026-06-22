package com.example.tracksy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.profile.ProfileAvatarImage
import com.example.tracksy.ui.theme.LocalTracksyColors

data class Product(
    val id: Long = 0L,
    val name: String,
    val category: String,
    val barcode: String? = null   // id.toString() — kept for display
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    selectedTab: NavTab,
    onTabChange: (NavTab) -> Unit,
    productosApi: List<Product> = emptyList(),
    favoritosApi: List<Product> = emptyList(),
    profilePhotoUri: String = "",
    onProductTap: (Product) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onToggleFavorito: (Long, Boolean) -> Unit = { _, _ -> },
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val colors = LocalTracksyColors.current
    val pullRefreshState = rememberPullToRefreshState()
    var searchQuery by remember { mutableStateOf("") }

    // IDs de favoritos para lookup O(1) y para filtrar la sección "Todos"
    val favoriteIds = remember(favoritosApi) { favoritosApi.map { it.id }.toSet() }

    val filteredFavorites = remember(favoritosApi, searchQuery) {
        favoritosApi.filter {
            searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    // "Todos los productos" excluye los ya favoritos para evitar duplicados
    val filteredAll = remember(productosApi, favoriteIds, searchQuery) {
        productosApi
            .filter { it.id !in favoriteIds }
            .filter {
                searchQuery.isBlank() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
            }
    }

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

                // ── Header ─────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Productos",
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
                        ProfileAvatarImage(
                            fotoUri = profilePhotoUri,
                            colors = colors,
                            iconSize = 24.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Buscador ───────────────────────────────────────────────────
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; onSearchChange(it) },
                    placeholder = {
                        Text("Buscar productos...", color = colors.subtitleText, fontSize = 15.sp)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = colors.primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(20.dp))

                // ── Mis favoritos ───────────────────────────────────────────────
                if (filteredFavorites.isNotEmpty()) {
                    Text(
                        text = "Mis favoritos ⭐",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.sectionText
                    )
                    Spacer(Modifier.height(10.dp))
                    ProductGroupCard(
                        products     = filteredFavorites,
                        favoriteIds  = favoriteIds,
                        onStarClick  = { product ->
                            onToggleFavorito(product.id, false)   // quitar de favoritos
                        },
                        onProductTap = onProductTap
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // ── Todos los productos ─────────────────────────────────────────
                if (filteredAll.isNotEmpty()) {
                    Text(
                        text = "Todos los productos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.sectionText
                    )
                    Spacer(Modifier.height(10.dp))
                    ProductGroupCard(
                        products     = filteredAll,
                        favoriteIds  = favoriteIds,
                        onStarClick  = { product ->
                            onToggleFavorito(product.id, true)    // agregar a favoritos
                        },
                        onProductTap = onProductTap
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // ── Estado vacío ────────────────────────────────────────────────
                if (filteredFavorites.isEmpty() && filteredAll.isEmpty()) {
                    Spacer(Modifier.height(48.dp))
                    Text(
                        text = "No se encontraron productos",
                        color = colors.subtitleText,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductGroupCard(
    products: List<Product>,
    favoriteIds: Set<Long>,
    onStarClick: (Product) -> Unit,
    onProductTap: (Product) -> Unit
) {
    val colors = LocalTracksyColors.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            products.forEachIndexed { index, product ->
                ProductRow(
                    product      = product,
                    isFavorito   = product.id in favoriteIds,
                    onStarClick  = { onStarClick(product) },
                    onOpenDetail = { onProductTap(product) }
                )
                if (index < products.lastIndex) {
                    HorizontalDivider(
                        color     = colors.divider,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    isFavorito: Boolean,
    onStarClick: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val colors = LocalTracksyColors.current
    val starColor = if (isFavorito) Color(0xFFFFC107) else colors.subtitleText  // ámbar si favorito

    // NO ponemos .clickable en el Row padre — usamos hermanos para evitar el conflicto
    // de eventos táctiles entre el área de detalle y el ícono de estrella.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        // ── Área clickable: nombre + categoría → abre detalle ──────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpenDetail)
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.titleText
            )
            if (product.category.isNotBlank()) {
                Text(
                    text = product.category,
                    fontSize = 12.sp,
                    color = colors.subtitleText
                )
            }
        }

        // ── Estrella — click independiente, no anidado en el Row ───────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)               // target táctil ≥ 44dp (HIG/MD)
                .clip(CircleShape)
                .clickable(onClick = onStarClick)
        ) {
            Icon(
                imageVector = if (isFavorito) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (isFavorito) "Quitar de favoritos" else "Agregar a favoritos",
                tint = starColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
