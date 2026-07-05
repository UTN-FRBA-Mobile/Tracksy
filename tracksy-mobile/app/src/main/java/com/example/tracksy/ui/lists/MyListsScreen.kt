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
import com.example.tracksy.screens.NavTab
import com.example.tracksy.screens.ShoppingList
import com.example.tracksy.screens.TracksyBottomBar
import com.example.tracksy.ui.components.TracksyTextAction
import com.example.tracksy.ui.profile.ProfileAvatarImage
import com.example.tracksy.ui.theme.LocalTracksyColors
import com.example.tracksy.ui.theme.TracksyPrimaryPurple
import com.example.tracksy.ui.theme.TracksyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisListasScreen(
    selectedTab: NavTab,
    onTabChange: (NavTab) -> Unit,
    listas: List<ShoppingList> = emptyList(),
    profilePhotoUri: String = "",
    onListClick: (ShoppingList) -> Unit = {},
    onCreateNewList: () -> Unit = {},
    onDeleteList: (ShoppingList) -> Unit = {},
    onProfileClick: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val colors = LocalTracksyColors.current
    var listPendingDeletionIndex by remember { mutableStateOf<Int?>(null) }
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = colors.background,
        bottomBar = { TracksyBottomBar(selected = selectedTab, onSelect = onTabChange) },
        floatingActionButton = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(TracksyPrimaryPurple)
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullRefreshState,
            modifier = Modifier.padding(innerPadding)
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
                    text = "Mis listas",
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

            if (listas.isEmpty()) {
                EmptyState(colors = colors, modifier = Modifier.fillMaxWidth().padding(top = 64.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 0.dp)
                ) {
                    itemsIndexed(items = listas) { index, list ->
                        ShoppingListCard(
                            list = list,
                            colors = colors,
                            onClick = { onListClick(list) },
                            onDelete = { listPendingDeletionIndex = index }
                        )
                    }
                }
            }
        }
        } // PullToRefreshBox
    }

    listPendingDeletionIndex?.let { index ->
        if (index in listas.indices) {
            DeleteListDialog(
                listName = listas[index].name,
                colors = colors,
                onConfirm = {
                    onDeleteList(listas[index])
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
    colors: TracksyColors,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val infoText = when {
        list.totalProducts == 0 -> "${list.totalProducts} productos · no hay productos pendientes"
        list.pendingProducts == list.totalProducts -> "${list.totalProducts} productos · todos pendientes"
        else -> "${list.totalProducts} productos · ${list.pendingProducts} pendientes"
    }

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
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = list.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colors.titleText)
                Spacer(Modifier.height(4.dp))
                Text(text = infoText, fontSize = 13.sp, color = colors.subtitleText)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onDelete)
            ) {
                Icon(imageVector = Icons.Outlined.Close, contentDescription = "Eliminar lista", tint = colors.errorRed, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun DeleteListDialog(
    listName: String,
    colors: TracksyColors,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.titleText,
        textContentColor = colors.subtitleText,
        title = { Text("¿Eliminar \"$listName\"?") },
        text = { Text("Esta acción no se puede deshacer.") },
        confirmButton = {
            TracksyTextAction(text = "Eliminar", onClick = onConfirm, contentColor = colors.errorRed)
        },
        dismissButton = {
            TracksyTextAction(text = "Cancelar", onClick = onDismiss, contentColor = colors.subtitleText)
        }
    )
}

@Composable
private fun EmptyState(colors: TracksyColors, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Todavía no tenés listas", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.titleText)
        Spacer(Modifier.height(6.dp))
        Text(text = "Tocá el + para crear tu primera lista", fontSize = 13.sp, color = colors.subtitleText)
    }
}
