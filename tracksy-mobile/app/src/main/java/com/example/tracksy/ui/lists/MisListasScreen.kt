package com.example.tracksy.ui.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.screens.NavTab
import com.example.tracksy.screens.ShoppingList
import com.example.tracksy.screens.TracksyBottomBar
import com.example.tracksy.ui.theme.*

@Composable
fun MisListasScreen(
    selectedTab: NavTab,
    onTabChange: (NavTab) -> Unit,
    onListClick: (ShoppingList) -> Unit = {},
    onCreateNewList: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var lists by remember {
        mutableStateOf(
            listOf(
                ShoppingList("Compra semanal", totalProducts = 8,  pendingProducts = 3),
                ShoppingList("Lista del mes",  totalProducts = 14, pendingProducts = 14),
                ShoppingList("Cumpleaños Juan", totalProducts = 6,  pendingProducts = 6)
            )
        )
    }
    var listPendingDeletionIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = TracksyBackground,
        bottomBar = { TracksyBottomBar(selected = selectedTab, onSelect = onTabChange) },
        floatingActionButton = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(TracksyPrimary)
                    .clickable(onClick = onCreateNewList)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Crear lista",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
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
                    text = "Mis listas",
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

            if (lists.isEmpty()) {
                EmptyState(modifier = Modifier.fillMaxWidth().padding(top = 64.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 0.dp)
                ) {
                    itemsIndexed(items = lists) { index, list ->
                        ShoppingListCard(
                            list = list,
                            onClick = { onListClick(list) },
                            onDelete = { listPendingDeletionIndex = index }
                        )
                    }
                }
            }
        }
    }

    listPendingDeletionIndex?.let { index ->
        if (index in lists.indices) {
            DeleteListDialog(
                listName = lists[index].name,
                onConfirm = {
                    lists = lists.toMutableList().also { it.removeAt(index) }
                    listPendingDeletionIndex = null
                },
                onDismiss = { listPendingDeletionIndex = null }
            )
        }
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingList,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val pendingLabel = if (list.pendingProducts == list.totalProducts)
        "todos pendientes"
    else
        "${list.pendingProducts} pendientes"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TracksySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TracksyTitleText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${list.totalProducts} productos · $pendingLabel",
                    fontSize = 13.sp,
                    color = TracksySubtitleText
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDelete)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Eliminar lista",
                    tint = TracksyPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DeleteListDialog(
    listName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TracksySurface,
        titleContentColor = TracksyTitleText,
        textContentColor = TracksySubtitleText,
        title = { Text("¿Eliminar \"$listName\"?") },
        text = { Text("Esta acción no se puede deshacer.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Eliminar",
                    color = TracksyErrorRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TracksySubtitleText)
            }
        }
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Todavía no tenés listas",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TracksyTitleText
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Tocá el + para crear tu primera lista",
            fontSize = 13.sp,
            color = TracksySubtitleText
        )
    }
}
