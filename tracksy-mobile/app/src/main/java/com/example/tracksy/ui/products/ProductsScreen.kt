package com.example.tracksy.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.LocalTracksyColors

data class Product(
    val id: Long = 0L,
    val name: String,
    val category: String,
    val barcode: String? = null   // id.toString() — kept for display ("Cód: …")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    selectedTab: NavTab,
    onTabChange: (NavTab) -> Unit,
    productosApi: List<Product> = emptyList(),
    favoritosApi: List<Product> = emptyList(),
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
    var favorites by remember(favoritosApi) { mutableStateOf(favoritosApi) }
    var allProducts by remember(productosApi) { mutableStateOf(productosApi) }

    var selectedFavorites by remember { mutableStateOf(emptySet<String>()) }
    var selectedAll       by remember { mutableStateOf(emptySet<String>()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val filteredFavorites = favorites.filter {
        searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
    }
    val filteredAll = allProducts.filter {
        searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
    }

    val hasFavSelected = selectedFavorites.isNotEmpty()
    val hasAllSelected = selectedAll.isNotEmpty()

    if (showConfirmDialog) {
        val count = selectedFavorites.size
        AlertDialog(
            onDismissRequest  = { showConfirmDialog = false },
            containerColor    = colors.surface,
            titleContentColor = colors.titleText,
            textContentColor  = colors.subtitleText,
            title = { Text("¿Quitar de favoritos?") },
            text  = {
                Text(
                    "Se quitará${if (count > 1) "n" else ""} $count " +
                    "producto${if (count > 1) "s" else ""} de tus favoritos " +
                    "y volverá${if (count > 1) "n" else ""} a la lista general."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val removed = favorites.filter { it.name in selectedFavorites }
                    removed.forEach { onToggleFavorito(it.id, false) }
                    favorites         = favorites.filter { it.name !in selectedFavorites }
                    allProducts       = allProducts + removed
                    selectedFavorites = emptySet()
                    showConfirmDialog  = false
                }) {
                    Text("Quitar", color = colors.errorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = colors.subtitleText)
                }
            }
        )
    }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            TracksyBottomBar(selected = selectedTab, onSelect = onTabChange)
        },
        floatingActionButton = {
            val fabColor = colors.primary
            when {
                hasAllSelected -> ProductsFab(color = fabColor, onClick = {
                    val toMove  = allProducts.filter { it.name in selectedAll }
                    toMove.forEach { onToggleFavorito(it.id, true) }
                    favorites   = favorites + toMove
                    allProducts = allProducts.filter { it.name !in selectedAll }
                    selectedAll = emptySet()
                }) {
                    Icon(imageVector = Icons.Outlined.Star, contentDescription = "Agregar a favoritos", tint = Color.White, modifier = Modifier.size(26.dp))
                }
                hasFavSelected -> ProductsFab(color = fabColor, onClick = { showConfirmDialog = true }) {
                    Box(modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Outlined.Star, contentDescription = "Quitar de favoritos", tint = Color.White, modifier = Modifier.size(26.dp))
                        Canvas(modifier = Modifier.size(26.dp)) {
                            drawLine(
                                color = Color.White,
                                start = Offset(2.dp.toPx(), size.height - 2.dp.toPx()),
                                end   = Offset(size.width - 2.dp.toPx(), 2.dp.toPx()),
                                strokeWidth = 2.5.dp.toPx(),
                                cap   = StrokeCap.Round
                            )
                        }
                    }
                }
                else -> ProductsFab(color = fabColor, onClick = { }) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Agregar producto", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Productos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.titleText)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(colors.divider).clickable(onClick = onProfileClick)
                ) {
                    Icon(imageVector = Icons.Outlined.Person, contentDescription = "Perfil", tint = colors.sectionText, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; onSearchChange(it) },
                placeholder = { Text("Buscar productos...", color = colors.subtitleText, fontSize = 15.sp) },
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

            if (filteredFavorites.isNotEmpty()) {
                Text(text = "Mis favoritos ⭐", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.sectionText)
                Spacer(Modifier.height(10.dp))
                ProductGroupCard(
                    products = filteredFavorites,
                    selected = selectedFavorites,
                    onToggle = { name ->
                        selectedFavorites = if (name in selectedFavorites) selectedFavorites - name else selectedFavorites + name
                    },
                    onProductTap = onProductTap
                )
                Spacer(Modifier.height(20.dp))
            }

            if (filteredAll.isNotEmpty()) {
                Text(text = "Todos los productos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.sectionText)
                Spacer(Modifier.height(10.dp))
                ProductGroupCard(
                    products = filteredAll,
                    selected = selectedAll,
                    onToggle = { name ->
                        selectedAll = if (name in selectedAll) selectedAll - name else selectedAll + name
                    },
                    onProductTap = onProductTap
                )
                Spacer(Modifier.height(16.dp))
            }

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
        } // PullToRefreshBox
    }
}

@Composable
private fun ProductsFab(color: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(56.dp).clip(CircleShape).background(color).clickable(onClick = onClick)
    ) {
        content()
    }
}

@Composable
private fun ProductGroupCard(
    products: List<Product>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
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
                    isSelected   = product.name in selected,
                    onToggle     = { onToggle(product.name) },
                    onOpenDetail = { onProductTap(product) }
                )
                if (index < products.lastIndex) {
                    HorizontalDivider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val colors = LocalTracksyColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggle)
                .then(
                    if (isSelected) Modifier.background(colors.primary)
                    else Modifier.border(1.5.dp, colors.subtitleText, CircleShape)
                )
        ) {
            if (isSelected) {
                Icon(imageVector = Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.width(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).clickable(onClick = onOpenDetail)
        ) {
            Text(text = product.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.titleText, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(colors.background).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = product.category, fontSize = 12.sp, color = colors.sectionText, fontWeight = FontWeight.Medium)
            }
        }
    }
}
