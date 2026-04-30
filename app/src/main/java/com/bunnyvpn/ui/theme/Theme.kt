package com.bunnyvpn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class AppTheme { DARK, LIGHT, NEON }

val LocalAppTheme = compositionLocalOf { AppTheme.DARK }

private val DarkColorScheme = darkColorScheme(
    primary = Cyan400,
    secondary = Purple400,
    background = BgBlack,
    surface = BgCard,
    onPrimary = BgBlack,
    onSecondary = BgBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = GlassBorder,
    tertiary = StatusConnected
)

private val LightColorScheme = lightColorScheme(
    primary = Cyan600,
    secondary = Purple600,
    background = LightBg,
    surface = LightCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    outline = Color(0xFFCCCCDD),
    tertiary = Color(0xFF00C853)
)

private val NeonColorScheme = darkColorScheme(
    primary = NeonPink,
    secondary = NeonGreen,
    background = Color(0xFF000010),
    surface = Color(0xFF0A0020),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0x44FF0080),
    tertiary = NeonGreen
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun BunnyVPNTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.DARK -> DarkColorScheme
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.NEON -> NeonColorScheme
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
