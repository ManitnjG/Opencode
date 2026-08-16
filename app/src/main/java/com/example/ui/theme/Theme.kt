package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OpenCodeCyan,
    onPrimary = Color(0xFF00354E),
    primaryContainer = Color(0xFF004D71),
    onPrimaryContainer = Color(0xFFC7E7FF),
    secondary = OpenCodeMint,
    onSecondary = Color(0xFF003918),
    secondaryContainer = Color(0xFF005325),
    onSecondaryContainer = Color(0xFF8CF8A4),
    tertiary = OpenCodePurple,
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = Color(0xFF4F378B),
    onTertiaryContainer = Color(0xFFEADBFF),
    error = OpenCodeRed,
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = OpenCodeBackground,
    onBackground = OpenCodeTextPrimary,
    surface = OpenCodeSurface,
    onSurface = OpenCodeTextPrimary,
    surfaceVariant = OpenCodeSurfaceVariant,
    onSurfaceVariant = OpenCodeTextSecondary,
    outline = OpenCodeBorder,
    outlineVariant = Color(0xFF263345)
)

private val LightColorScheme = lightColorScheme(
    primary = OpenCodeCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF082F49),
    secondary = OpenCodeMintDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF14532D),
    tertiary = OpenCodePurpleDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF581C87),
    error = Color(0xFFDC2626),
    onError = Color.White,
    background = OpenCodeLightBackground,
    onBackground = OpenCodeLightTextPrimary,
    surface = OpenCodeLightSurface,
    onSurface = OpenCodeLightTextPrimary,
    surfaceVariant = OpenCodeLightSurfaceVariant,
    onSurfaceVariant = OpenCodeLightTextSecondary,
    outline = OpenCodeLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun OpenCodeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, content = content)
}

