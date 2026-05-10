package com.example.tracksy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

object TracksyAuthTypography {
    private const val AuthFontScale = 0.5f

    val Brand = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = (64 * AuthFontScale).sp,
        lineHeight = (72 * AuthFontScale).sp,
        letterSpacing = 0.sp
    )

    val ScreenTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = (40 * AuthFontScale).sp,
        lineHeight = (48 * AuthFontScale).sp,
        letterSpacing = (0.34 * AuthFontScale).sp
    )

    val Button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = (36 * AuthFontScale).sp,
        lineHeight = (44 * AuthFontScale).sp,
        letterSpacing = (0.34 * AuthFontScale).sp
    )

    val Field = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = (25 * AuthFontScale).sp,
        lineHeight = (32 * AuthFontScale).sp,
        letterSpacing = (0.34 * AuthFontScale).sp
    )

    val Body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = (20 * AuthFontScale).sp,
        lineHeight = (24 * AuthFontScale).sp,
        letterSpacing = (1 * AuthFontScale).sp
    )

    val Link = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = (20 * AuthFontScale).sp,
        lineHeight = (24 * AuthFontScale).sp,
        letterSpacing = 0.sp
    )

    val Helper = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = (16 * AuthFontScale).sp,
        lineHeight = (20 * AuthFontScale).sp,
        letterSpacing = (0.8 * AuthFontScale).sp
    )
}
