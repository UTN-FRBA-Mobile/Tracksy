package com.example.tracksy.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracksy.R
import com.example.tracksy.ui.components.TracksyErrorMessage as SharedTracksyErrorMessage
import com.example.tracksy.ui.components.TracksyPasswordField as SharedTracksyPasswordField
import com.example.tracksy.ui.components.TracksyPrimaryButton as SharedTracksyPrimaryButton
import com.example.tracksy.ui.components.TracksySecondaryButton as SharedTracksySecondaryButton
import com.example.tracksy.ui.components.TracksyTextField as SharedTracksyTextField
import com.example.tracksy.ui.theme.TracksyBorderSoft
import com.example.tracksy.ui.theme.TracksyDisabledButtonBackground
import com.example.tracksy.ui.theme.TracksyDisabledButtonText
import com.example.tracksy.ui.theme.TracksyErrorRed
import com.example.tracksy.ui.theme.TracksyPlaceholder
import com.example.tracksy.ui.theme.TracksyPrimaryPurple
import com.example.tracksy.ui.theme.TracksySurfaceCard
import com.example.tracksy.ui.theme.TracksyTextMuted
import com.example.tracksy.ui.theme.TracksyTextPrimary
import com.example.tracksy.ui.theme.TracksyTextSecondary

private val AuthFieldShape = RoundedCornerShape(10.dp)
private val AuthButtonShape = RoundedCornerShape(25.dp)

private val BrandTextStyle = TextStyle(
    fontWeight = FontWeight.Black,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = 0.sp
)

private val WelcomeBrandTextStyle = TextStyle(
    fontWeight = FontWeight.Black,
    fontSize = 52.sp,
    lineHeight = 60.sp,
    letterSpacing = 0.sp
)

private val SubtitleTextStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

private val FieldTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.5.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp
)

private val ButtonTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp
)

private val LinkTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.sp
)

@Composable
fun TracksyAuthBackground(
    modifier: Modifier = Modifier,
    imageAlpha: Float = 0.55f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.auth_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = imageAlpha
        )
        content()
    }
}

@Composable
fun AuthCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 17.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = TracksySurfaceCard
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            content()
        }
    }
}

@Composable
fun AuthScreenContainer(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = 47.dp,
        top = 34.dp,
        end = 47.dp,
        bottom = 24.dp
    ),
    content: @Composable () -> Unit
) {
    TracksyAuthBackground(modifier = modifier, imageAlpha = 0.34f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            AuthCard {
                content()
            }
        }
    }
}

@Composable
fun AuthHeader(
    title: String,
    modifier: Modifier = Modifier,
    appName: String = "Tracksy"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = appName,
            color = TracksyPrimaryPurple,
            style = BrandTextStyle,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = title,
            color = TracksyTextSecondary,
            style = SubtitleTextStyle,
            textAlign = TextAlign.Center
        )
    }
}

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
    SharedTracksyTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        onFocusChanged = onFocusChanged,
        keyboardType = keyboardType,
        imeAction = imeAction,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingContent = trailingContent
    )
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
    SharedTracksyPasswordField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        onFocusChanged = onFocusChanged,
        imeAction = imeAction,
        keyboardActions = keyboardActions
    )
}

@Composable
private fun PasswordVisibilityButton(
    visible: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            val iconColor = if (visible) TracksyTextSecondary else TracksyPrimaryPurple
            drawOval(
                color = iconColor,
                topLeft = Offset(size.width * 0.12f, size.height * 0.28f),
                size = Size(size.width * 0.76f, size.height * 0.44f),
                style = stroke
            )
            drawCircle(
                color = iconColor,
                radius = size.minDimension * 0.12f,
                center = center,
                style = stroke
            )
            if (!visible) {
                drawLine(
                    color = iconColor,
                    start = Offset(size.width * 0.10f, size.height * 0.14f),
                    end = Offset(size.width * 0.90f, size.height * 0.86f),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun TracksyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    SharedTracksyPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
fun TracksySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    SharedTracksySecondaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
fun TracksyLinkText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        color = TracksyPrimaryPurple,
        style = LinkTextStyle.copy(fontWeight = FontWeight.Medium),
        textAlign = TextAlign.Center
    )
}

@Composable
fun TracksyInlineLink(
    text: String,
    linkText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = TracksyTextSecondary, fontWeight = FontWeight.Medium)) {
                append(text)
            }
            append(" ")
            withStyle(SpanStyle(color = TracksyPrimaryPurple, fontWeight = FontWeight.SemiBold)) {
                append(linkText)
            }
        },
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        style = LinkTextStyle,
        textAlign = TextAlign.Center
    )
}

@Composable
fun ErrorMessage(
    text: String,
    modifier: Modifier = Modifier
) {
    SharedTracksyErrorMessage(text = text, modifier = modifier)
}

@Composable
fun AuthOrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = TracksyBorderSoft)
        Text(
            text = "  o  ",
            color = TracksyTextMuted,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.sp
            )
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = TracksyBorderSoft)
    }
}

@Composable
fun TracksyGoogleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .shadow(2.dp, AuthButtonShape, clip = false),
        enabled = enabled && !isLoading,
        shape = AuthButtonShape,
        border = BorderStroke(1.dp, TracksyBorderSoft),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.94f),
            contentColor = TracksyTextPrimary,
            disabledContainerColor = Color.White.copy(alpha = 0.72f),
            disabledContentColor = TracksyDisabledButtonText
        ),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        if (isLoading) {
            Text(text = "Conectando...", style = ButtonTextStyle, maxLines = 1)
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text(text = "Continuar con Google", style = ButtonTextStyle, maxLines = 1)
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = FieldTextStyle,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TracksyTextField(
        value = value,
        onValueChange = onValueChange,
        label = placeholder,
        modifier = modifier.widthIn(max = 254.dp),
        enabled = enabled,
        visualTransformation = visualTransformation
    )
}

@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TracksyPasswordField(
        value = value,
        onValueChange = onValueChange,
        label = placeholder,
        modifier = modifier.widthIn(max = 254.dp),
        enabled = enabled
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dark: Boolean = false
) {
    TracksyPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier.widthIn(max = 254.dp),
        enabled = enabled
    )
}

@Composable
fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TracksySecondaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier.widthIn(max = 254.dp),
        enabled = enabled
    )
}

@Composable
fun AuthLinkText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TracksyLinkText(text = text, onClick = onClick, modifier = modifier)
}

@Composable
fun AuthInlineLinkText(
    text: String,
    linkText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TracksyInlineLink(text = text, linkText = linkText, onClick = onClick, modifier = modifier)
}

@Composable
fun AuthSuccessMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    helperText: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            color = TracksyTextSecondary,
            style = SubtitleTextStyle,
            textAlign = TextAlign.Center
        )
        Text(
            text = body,
            color = TracksyTextSecondary,
            style = FieldTextStyle
        )
        helperText?.let {
            Text(
                text = it,
                color = TracksyTextMuted,
                style = LinkTextStyle
            )
        }
    }
}

@Composable
fun WelcomeBrand(modifier: Modifier = Modifier) {
    Text(
        text = "Tracksy",
        modifier = modifier.fillMaxWidth(),
        color = TracksyPrimaryPurple,
        style = WelcomeBrandTextStyle,
        textAlign = TextAlign.Center
    )
}
