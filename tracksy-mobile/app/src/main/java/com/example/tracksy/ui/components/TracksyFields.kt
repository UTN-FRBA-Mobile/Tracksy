package com.example.tracksy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.ui.theme.TracksyBorderSoft
import com.example.tracksy.ui.theme.TracksyDisabledButtonBackground
import com.example.tracksy.ui.theme.TracksyErrorRed
import com.example.tracksy.ui.theme.TracksyPlaceholder
import com.example.tracksy.ui.theme.TracksyPrimaryPurple
import com.example.tracksy.ui.theme.TracksyTextMuted
import com.example.tracksy.ui.theme.TracksyTextPrimary
import com.example.tracksy.ui.theme.TracksyTextSecondary

private val TracksyFieldShape = RoundedCornerShape(10.dp)

private val TracksyFieldTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.5.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp
)

@Composable
fun TracksyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onFocusChanged: (Boolean) -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val showLabel = isFocused || value.isNotEmpty()
    val borderColor = when {
        isError -> TracksyErrorRed
        !enabled -> TracksyBorderSoft.copy(alpha = 0.22f)
        else -> TracksyBorderSoft.copy(alpha = 0.35f)
    }
    val fieldBackground = if (enabled) Color.White else TracksyDisabledButtonBackground.copy(alpha = 0.35f)

    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = showLabel,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = label,
                color = if (enabled) TracksyTextSecondary else TracksyTextMuted,
                style = TracksyFieldTextStyle,
                modifier = Modifier.padding(start = 15.dp, bottom = 5.dp)
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .shadow(2.dp, TracksyFieldShape, clip = false)
                .border(1.dp, borderColor, TracksyFieldShape),
            shape = TracksyFieldShape,
            color = fieldBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 15.dp, end = if (trailingContent == null) 15.dp else 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            isFocused = it.isFocused
                            onFocusChanged(it.isFocused)
                        },
                    enabled = enabled,
                    singleLine = true,
                    textStyle = TracksyFieldTextStyle.copy(
                        color = if (enabled) TracksyTextPrimary else TracksyTextMuted
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    ),
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isEmpty() && !isFocused) {
                                Text(
                                    text = label,
                                    color = if (enabled) TracksyPlaceholder else TracksyTextMuted,
                                    style = TracksyFieldTextStyle
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                trailingContent?.invoke()
            }
        }
    }
}

@Composable
fun TracksyPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onFocusChanged: (Boolean) -> Unit = {},
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    TracksyTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        onFocusChanged = onFocusChanged,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        keyboardActions = keyboardActions,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingContent = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(
                        enabled = enabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { passwordVisible = !passwordVisible }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                    tint = if (passwordVisible) TracksyTextSecondary else TracksyPrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

@Composable
fun TracksyErrorMessage(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Canvas(
            modifier = Modifier
                .padding(top = 1.dp)
                .size(14.dp)
        ) {
            drawLine(
                color = TracksyErrorRed,
                start = Offset(size.width * 0.18f, size.height * 0.18f),
                end = Offset(size.width * 0.82f, size.height * 0.82f),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = TracksyErrorRed,
                start = Offset(size.width * 0.82f, size.height * 0.18f),
                end = Offset(size.width * 0.18f, size.height * 0.82f),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = TracksyErrorRed,
            style = TracksyFieldTextStyle.copy(fontWeight = FontWeight.Medium),
            maxLines = 3
        )
    }
}
