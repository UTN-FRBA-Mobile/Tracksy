package com.example.tracksy.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Material baseline (kept for theme compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Tracksy brand colors
val BackgroundLavender = Color(0xFFEDE8FF)
val PrimaryDeepPurple = Color(0xFF2E1B7E)
val MediumPurple = Color(0xFF7B6FD2)
val ButtonPink = Color(0xFFE9487A)
val BadgeGreenBg = Color(0xFFE8F5E9)
val BadgeGreenText = Color(0xFF2E7D32)
val BadgeOrangeBg = Color(0xFFFFF3E0)
val BadgeOrangeText = Color(0xFFE65100)
val CardWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1C1B1F)
val TextSecondaryPurple = Color(0xFF6B5EA8)
val PlaceholderGray = Color(0xFFAEABBE)
val DashedBorderGray = Color(0xFFADADB3)
val PurchasedText = Color(0xFF9E9EC8)
val LocationPinRed = Color(0xFFE53935)

val TracksyPrimaryPurple = Color(0xFF6F3F97)
val TracksyPrimaryDark  = Color(0xFF5A317E)
val TracksyTextPrimary  = Color(0xFF3F245F)
val TracksyTextSecondary = Color(0xFF7E669A)
val TracksyTextMuted    = Color(0xFFA895C0)
val TracksyPlaceholder  = Color(0xFF9279AD)
val TracksySurfaceCard  = Color(0xE6FFFFFF)
val TracksyBorderSoft   = Color(0xFFE2D8EE)
val TracksyDisabledButtonBackground = Color(0xFFEEDDF0)
val TracksyDisabledButtonText = Color(0xFF6F4B8B)
val TracksyErrorRed     = Color(0xFFC62828)
val TracksySuccessGreen = Color(0xFF388E3C)
val TracksyChecklistNeutral = Color(0xFF6F5F7D)

val TracksyBackgroundLight = Color(0xFFF5F0FA)
val TracksySuccessBadgeBackground = Color(0xFFE8F5E9)
val TracksyWarningBadgeBackground = Color(0xFFFFF9C4)
val TracksyWarningText  = Color(0xFFFBC02D)

val TracksyBrandPurple       = TracksyPrimaryPurple
val TracksyDarkPrimaryPurple = TracksyPrimaryDark
val TracksySoftPrimary       = TracksyDisabledButtonBackground
val TracksySecondaryText     = TracksyTextSecondary
val TracksyPanelBackground   = TracksySurfaceCard
val TracksyFieldPlaceholder  = TracksyPlaceholder

// Tracksy design tokens aligned to the auth screens.
val TracksyBackground   = TracksyBackgroundLight
val TracksyPrimary      = TracksyPrimaryPurple
val TracksySurface      = Color.White
val TracksyTitleText    = TracksyTextPrimary
val TracksySectionText  = TracksyPrimaryPurple
val TracksySubtitleText = TracksyTextSecondary
val TracksyDivider      = TracksyBorderSoft
val TracksyNavActive    = TracksyPrimaryPurple
val TracksyNavInactive  = TracksyTextMuted
val TracksyQrBorder     = TracksyDisabledButtonBackground

// ─── Dynamic color tokens (used by LocalTracksyColors) ──────────────────────

data class TracksyColors(
    val background: Color,
    val surface: Color,
    val titleText: Color,
    val sectionText: Color,
    val subtitleText: Color,
    val primary: Color,
    val divider: Color,
    val errorRed: Color,
    val successGreen: Color,
    val successBadgeBackground: Color,
    val warningBadgeBackground: Color,
    val warningText: Color,
    val navActive: Color,
    val navInactive: Color,
    val isDark: Boolean
)

fun lightTracksyColors() = TracksyColors(
    background            = TracksyBackgroundLight,
    surface               = TracksySurface,
    titleText             = TracksyTextPrimary,
    sectionText           = TracksyPrimaryPurple,
    subtitleText          = TracksyTextSecondary,
    primary               = TracksyPrimaryPurple,
    divider               = TracksyBorderSoft,
    errorRed              = TracksyErrorRed,
    successGreen          = TracksySuccessGreen,
    successBadgeBackground = Color(0xFFE8F5E9),
    warningBadgeBackground = Color(0xFFFFF9C4),
    warningText           = Color(0xFFFBC02D),
    navActive             = TracksyPrimaryPurple,
    navInactive           = TracksyTextMuted,
    isDark                = false
)

fun darkTracksyColors() = TracksyColors(
    background            = Color(0xFF231730),
    surface               = Color(0xFF342244),
    titleText             = Color(0xFFF6ECFF),
    sectionText           = Color(0xFFD8C0EA),
    subtitleText          = Color(0xFFC1A4D7),
    primary               = Color(0xFFE2B7F3),
    divider               = Color(0xFF51335F),
    errorRed              = Color(0xFFEF5350),
    successGreen          = Color(0xFF66BB6A),
    successBadgeBackground = Color(0xFF1B3A1C),
    warningBadgeBackground = Color(0xFF3A3000),
    warningText           = Color(0xFFFFD54F),
    navActive             = Color(0xFFF6ECFF),
    navInactive           = Color(0xFFA895C0),
    isDark                = true
)

val LocalTracksyColors = compositionLocalOf { lightTracksyColors() }
