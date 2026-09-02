package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private fun createCustomLightColorScheme(primary: Color, container: Color): ColorScheme {
    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = container,
        onPrimaryContainer = Color(0xFF00201A),
        secondary = primary,
        onSecondary = Color.White,
        secondaryContainer = container,
        background = MintFoundation,
        surface = MintSurface,
        onBackground = Color(0xFF182320),
        onSurface = Color(0xFF182320),
        surfaceVariant = SoftCreamCard,
        onSurfaceVariant = Color(0xFF3F4946)
    )
}

private fun createCustomDarkColorScheme(primary: Color, container: Color): ColorScheme {
    return darkColorScheme(
        primary = primary,
        onPrimary = Color(0xFF00382E),
        primaryContainer = container,
        onPrimaryContainer = Color(0xFF8CF4D6),
        secondary = primary,
        background = DarkBackground,
        surface = DarkSurface,
        onBackground = Color(0xFFE0E3E1),
        onSurface = Color(0xFFE0E3E1),
        surfaceVariant = DarkCardSurface,
        onSurfaceVariant = Color(0xFFBEC9C5)
    )
}

@Composable
fun DaywillTheme(
    themeName: String = "Classic Teal",
    appearanceMode: String = "System", // System, Light, Dark
    content: @Composable () -> Unit
) {
    val isDark = when (appearanceMode) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    val context = LocalContext.current

    val colorScheme = when {
        themeName == "Dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val (primaryLight, containerLight) = when (themeName) {
                "Mint" -> Pair(MintPrimary, MintContainer)
                "Lavender" -> Pair(LavenderPrimary, LavenderContainer)
                "Peach" -> Pair(PeachPrimary, PeachContainer)
                "Ocean" -> Pair(OceanPrimary, OceanContainer)
                "Sunset" -> Pair(SunsetPrimary, SunsetContainer)
                "Forest" -> Pair(ForestPrimary, ForestContainer)
                else -> Pair(TealPrimary, TealContainer) // Classic Teal
            }

            if (isDark) {
                createCustomDarkColorScheme(primaryLight, DarkTealContainer)
            } else {
                createCustomLightColorScheme(primaryLight, containerLight)
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
