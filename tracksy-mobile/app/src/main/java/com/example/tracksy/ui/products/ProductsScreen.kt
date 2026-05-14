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
import com.example.tracksy.ui.theme.*

data class Product(
    val name: String,
    val category: String,
    val barcode: String? = null
)

@Composable
fun ProductsScreen(
    selectedTab: NavTab,
    onTabChange: (NavTab) -> Unit,
    onProductTap: (Product) -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var favorites by remember {
        mutableStateOf(
            listOf(
                Product("Leche entera La Serenísima x1L", "Lácteos", "7790895000028"),
                Product("Pan lactal Bimbo", "Panadería", "7790040152205")
            )
        )
    }
    var allProducts by remember {
        mutableStateOf(
            listOf(
                Product("Arroz largo fino Molinos x1kg", "Secos", "7790040004003"),
                Product("Aceite de girasol Cocinero x1.5L", "Secos"),
                Product("Yogur natural Danone", "Lácteos")
            )
        )
    }

    var selectedFavorites by remember { mutableStateOf(emptySet<String>()) }
    var selectedAll      by remember { mutableStateOf(emptySet<String>()) }
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
    val hasAllSelected  = selectedAll.isNotEmpty()

    if (showConfirmDialog) {
        val count = selectedFavorites.size
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor   = TracksySurface,
            titleContentColor = TracksyTitleText,
            textContentColor  = TracksySubtitleText,
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
                    favorites    = favorites.filter { it.name !in selectedFavorites }
                    allProducts  = allProducts + removed
                    selectedFavorites = emptySet()
                    showConfirmDialog  = false
                }) {
                    Text("Quitar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = TracksySubtitleText)
                }
            }
        )
    }

    Scaffold(
        containerColor = TracksyBackground,
        bottomBar = {
            TracksyBottomBar(selected = selectedTab, onSelect = onTabChange)
        },
        floatingActionButton = {
            when {
                // Any item from "todos" selected → add to favorites (no confirmation)
                hasAllSelected -> ProductsFab(onClick = {
                    val toMove  = allProducts.filter { it.name in selectedAll }
                    favorites   = favorites + toMove
                    allProducts = allProducts.filter { it.name !in selectedAll }
                    selectedAll = emptySet()
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Agregar a favoritos",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                // Only favorites selected → crossed star (remove, with confirmation)
                hasFavSelected -> ProductsFab(onClick = { showConfirmDialog = true }) {
                    Box(modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = "Quitar de favoritos",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Canvas(modifier = Modifier.size(26.dp)) {
                            drawLine(
                                color  = Color.White,
                                start  = Offset(2.dp.toPx(), size.height - 2.dp.toPx()),
                                end    = Offset(size.width - 2.dp.toPx(), 2.dp.toPx()),
                                strokeWidth = 2.5.dp.toPx(),
                                cap    = StrokeCap.Round
                            )
                        }
                    }
                }
                // Nothing selected → regular add button
                else -> ProductsFab(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Agregar producto",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Productos",
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
                        .clickable(onClick = onProfileClick)
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

            // Functional search bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Buscar productos...", color = TracksySubtitleText, fontSize = 15.sp)
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = TracksySurface,
                    unfocusedContainerColor = TracksySurface,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = TracksyPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))

            // Mis favoritos
            if (filteredFavorites.isNotEmpty()) {
                Text(
                    text = "Mis favoritos ⭐",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TracksySectionText
                )
                Spacer(Modifier.height(10.dp))
                ProductGroupCard(
                    products = filteredFavorites,
                    selected = selectedFavorites,
                    onToggle = { name ->
                        selectedFavorites = if (name in selectedFavorites)
                            selectedFavorites - name else selectedFavorites + name
                    },
                    onProductTap = onProductTap
                )
                Spacer(Modifier.height(20.dp))
            }

            // Todos los productos
            if (filteredAll.isNotEmpty()) {
                Text(
                    text = "Todos los productos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TracksySectionText
                )
                Spacer(Modifier.height(10.dp))
                ProductGroupCard(
                    products = filteredAll,
                    selected = selectedAll,
                    onToggle = { name ->
                        selectedAll = if (name in selectedAll)
                            selectedAll - name else selectedAll + name
                    },
                    onProductTap = onProductTap
                )
                Spacer(Modifier.height(16.dp))
            }

            if (filteredFavorites.isEmpty() && filteredAll.isEmpty()) {
                Spacer(Modifier.height(48.dp))
                Text(
                    text = "No se encontraron productos",
                    color = TracksySubtitleText,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun ProductsFab(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(TracksyPrimary)
            .clickable(onClick = onClick)
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
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = TracksySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
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
                    HorizontalDivider(
                        color     = TracksyDivider,
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
    isSelected: Boolean,
    onToggle: () -> Unit,
    onOpenDetail: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Circle: only toggles selection
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggle)
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
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        // Name + category: opens detail screen
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpenDetail)
        ) {
            Text(
                text       = product.name,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = TracksyTitleText,
                modifier   = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(TracksyBackground)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text       = product.category,
                    fontSize   = 12.sp,
                    color      = TracksySectionText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
