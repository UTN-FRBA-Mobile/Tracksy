package com.example.tracksy.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.*

private val HistoryTitleStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    color = TracksyTitleText
)

private val CardTitleStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    color = TracksyTitleText
)

private val CardAmountStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    color = TracksyTitleText
)

private val CardSubtitleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    color = TracksyTextSecondary
)

private val BadgeTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksyTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = HistoryTitleStyle
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TracksyPrimaryPurple
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Perfil */ }) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(TracksyDisabledButtonBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = TracksyPrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    )
}

@Composable
fun HistoryCard(
    item: HistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.supermarketName,
                    style = CardTitleStyle
                )
                Text(
                    text = item.totalAmount,
                    style = CardAmountStyle
                )
            }
            
            Text(
                text = "${item.dateLabel} · ${item.productCount} productos",
                style = CardSubtitleStyle
            )
            
            StatusBadge(status = item.status, pendingCount = item.pendingCount)
        }
    }
}

@Composable
fun StatusBadge(
    status: PurchaseStatus,
    pendingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (status == PurchaseStatus.COMPLETED) 
        TracksySuccessBadgeBackground else TracksyWarningBadgeBackground
    val textColor = if (status == PurchaseStatus.COMPLETED) 
        TracksySuccessGreen else TracksyWarningText
    val text = if (status == PurchaseStatus.COMPLETED) 
        "Completada" else "Incompleta ($pendingCount pendientes)"

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            style = BadgeTextStyle,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ProductItemRow(
    product: Product,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = if (product.isCompleted) Icons.Default.Check else Icons.Default.Close
        val color = if (product.isCompleted) TracksyPrimary else TracksyErrorRed
        
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = color
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(4.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = product.name,
            style = CardSubtitleStyle.copy(
                fontSize = 14.sp, 
                color = if (product.isCompleted) TracksyPrimary else TracksyTextSecondary.copy(alpha = 0.6f)
            ),
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = product.price,
            style = CardAmountStyle.copy(
                fontSize = 14.sp,
                color = if (product.isCompleted) TracksyPrimary else TracksyErrorRed.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.End
        )
    }
}
