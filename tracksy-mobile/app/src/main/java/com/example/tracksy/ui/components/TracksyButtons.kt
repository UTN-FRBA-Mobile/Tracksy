package com.example.tracksy.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.tracksy.ui.theme.TracksyBorderSoft
import com.example.tracksy.ui.theme.TracksyDisabledButtonBackground
import com.example.tracksy.ui.theme.TracksyDisabledButtonText
import com.example.tracksy.ui.theme.TracksyErrorRed
import com.example.tracksy.ui.theme.TracksyPrimaryPurple

private val TracksyButtonShape = RoundedCornerShape(25.dp)

private val TracksyButtonTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp
)

private val TracksyActionTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.sp
)

@Composable
fun TracksyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .shadow(2.dp, TracksyButtonShape, clip = false),
        enabled = enabled,
        shape = TracksyButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = TracksyPrimaryPurple,
            contentColor = Color.White,
            disabledContainerColor = TracksyDisabledButtonBackground,
            disabledContentColor = TracksyDisabledButtonText
        ),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(text = text, style = TracksyButtonTextStyle, maxLines = 1)
    }
}

@Composable
fun TracksySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .shadow(2.dp, TracksyButtonShape, clip = false),
        enabled = enabled,
        shape = TracksyButtonShape,
        border = BorderStroke(1.dp, TracksyBorderSoft),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.94f),
            contentColor = TracksyPrimaryPurple,
            disabledContainerColor = Color.White.copy(alpha = 0.72f),
            disabledContentColor = TracksyDisabledButtonText
        ),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(text = text, style = TracksyButtonTextStyle, maxLines = 1)
    }
}

@Composable
fun TracksyPrimaryButtonSmall(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        enabled = enabled,
        shape = TracksyButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = TracksyPrimaryPurple,
            contentColor = Color.White,
            disabledContainerColor = TracksyDisabledButtonBackground,
            disabledContentColor = TracksyDisabledButtonText
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun TracksySecondaryButtonSmall(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        enabled = enabled,
        shape = TracksyButtonShape,
        border = BorderStroke(1.dp, TracksyBorderSoft),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.94f),
            contentColor = TracksyPrimaryPurple,
            disabledContainerColor = Color.White.copy(alpha = 0.72f),
            disabledContentColor = TracksyDisabledButtonText
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
fun TracksyDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        enabled = enabled,
        shape = TracksyButtonShape,
        border = BorderStroke(1.dp, TracksyErrorRed.copy(alpha = if (enabled) 1f else 0.32f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.72f),
            contentColor = TracksyErrorRed,
            disabledContainerColor = Color.White.copy(alpha = 0.56f),
            disabledContentColor = TracksyErrorRed.copy(alpha = 0.42f)
        ),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = TracksyButtonTextStyle, maxLines = 1)
    }
}

@Composable
fun TracksyTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    contentColor: Color = TracksyPrimaryPurple
) {
    val resolvedContentColor = if (enabled) contentColor else TracksyDisabledButtonText
    Row(
        modifier = modifier.clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = resolvedContentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = resolvedContentColor,
            style = TracksyActionTextStyle,
            textAlign = TextAlign.Center
        )
    }
}
