package com.example.tracksy.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.tracksy.R
import com.example.tracksy.ui.theme.TracksyAuthTypography
import com.example.tracksy.ui.theme.TracksyBrandPurple
import com.example.tracksy.ui.theme.TracksyDarkPrimaryPurple
import com.example.tracksy.ui.theme.TracksyFieldPlaceholder
import com.example.tracksy.ui.theme.TracksyPanelBackground
import com.example.tracksy.ui.theme.TracksyPrimaryPurple
import com.example.tracksy.ui.theme.TracksySecondaryText
import com.example.tracksy.ui.theme.TracksySoftPrimary

@Composable
fun AuthScreenContainer(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = 52.dp,
        top = 40.dp,
        end = 52.dp,
        bottom = 37.dp
    ),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.auth_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = TracksyPanelBackground
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 17.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    content()
                }
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
            color = TracksyBrandPurple,
            style = TracksyAuthTypography.Brand,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = title,
            color = TracksyFieldPlaceholder,
            style = TracksyAuthTypography.ScreenTitle,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = TracksyAuthTypography.Field,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 254.dp)
            .height(44.dp)
            .shadow(2.dp, RoundedCornerShape(10.dp), clip = false),
        shape = RoundedCornerShape(10.dp),
        color = Color.White
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp),
            enabled = enabled,
            singleLine = true,
            textStyle = textStyle.copy(color = TracksyPrimaryPurple),
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TracksyFieldPlaceholder,
                            style = textStyle
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    AuthTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        enabled = enabled,
        visualTransformation = PasswordVisualTransformation()
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
    val containerColor = if (dark) TracksyDarkPrimaryPurple else TracksySoftPrimary
    val contentColor = if (dark) Color.White else TracksyPrimaryPurple

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 254.dp)
            .height(44.dp)
            .shadow(2.dp, RoundedCornerShape(25.dp), clip = false),
        enabled = enabled,
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(
            text = text,
            style = TracksyAuthTypography.Button,
            maxLines = 1
        )
    }
}

@Composable
fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = TracksyPrimaryPurple)
    ) {
        Text(
            text = text,
            style = TracksyAuthTypography.Link
        )
    }
}

@Composable
fun AuthLinkText(
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
        style = TracksyAuthTypography.Link,
        textAlign = TextAlign.Center
    )
}

@Composable
fun AuthInlineLinkText(
    text: String,
    linkText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = TracksySecondaryText, fontWeight = FontWeight.Medium)) {
                append(text)
            }
            append(" ")
            withStyle(SpanStyle(color = TracksyPrimaryPurple, fontWeight = FontWeight.Bold)) {
                append(linkText)
            }
        },
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        style = TracksyAuthTypography.Link,
        textAlign = TextAlign.Center
    )
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
            color = TracksyFieldPlaceholder,
            style = TracksyAuthTypography.ScreenTitle,
            textAlign = TextAlign.Center
        )
        Text(
            text = body,
            color = TracksySecondaryText,
            style = TracksyAuthTypography.Body
        )
        helperText?.let {
            Text(
                text = it,
                color = TracksySecondaryText,
                style = TracksyAuthTypography.Helper
            )
        }
    }
}
