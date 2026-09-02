package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val screenPadding: Dp = 16.dp,
    val cardPadding: Dp = 18.dp,
    val itemSpacing: Dp = 12.dp,
    val sectionSpacing: Dp = 16.dp,
    val cornerRadiusLarge: Dp = 28.dp,
    val cornerRadiusMedium: Dp = 20.dp,
    val cornerRadiusSmall: Dp = 12.dp,
    val iconSizeLarge: Dp = 48.dp,
    val iconSizeMedium: Dp = 24.dp,
    val iconSizeSmall: Dp = 18.dp,
    val timerSize: Dp = 250.dp,
    val buttonHeight: Dp = 52.dp,
    val displayFontScale: Float = 1.0f
)

val CompactDimensions = Dimensions(
    screenPadding = 10.dp,
    cardPadding = 12.dp,
    itemSpacing = 8.dp,
    sectionSpacing = 10.dp,
    cornerRadiusLarge = 20.dp,
    cornerRadiusMedium = 14.dp,
    cornerRadiusSmall = 8.dp,
    iconSizeLarge = 36.dp,
    iconSizeMedium = 20.dp,
    iconSizeSmall = 16.dp,
    timerSize = 190.dp,
    buttonHeight = 44.dp,
    displayFontScale = 0.85f
)

val StandardDimensions = Dimensions()

val LocalAppDimensions = staticCompositionLocalOf { StandardDimensions }

@Composable
fun ProvideAppDimensions(
    isCompactMode: Boolean,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    // Auto-detect small display devices or user explicit Compact Mode toggle
    val isSmallDevice = screenWidthDp < 360.dp || screenHeightDp < 660.dp
    val activeCompact = isCompactMode || isSmallDevice

    val baseDimensions = if (activeCompact) CompactDimensions else StandardDimensions

    // Scale dynamically for ultra-compact (< 340dp) or wide displays
    val scaleFactor = (screenWidthDp.value / 390f).coerceIn(0.78f, 1.25f)

    val scaledDimensions = baseDimensions.copy(
        screenPadding = (baseDimensions.screenPadding.value * scaleFactor).dp.coerceIn(8.dp, 20.dp),
        cardPadding = (baseDimensions.cardPadding.value * scaleFactor).dp.coerceIn(10.dp, 22.dp),
        itemSpacing = (baseDimensions.itemSpacing.value * scaleFactor).dp.coerceIn(6.dp, 16.dp),
        timerSize = (baseDimensions.timerSize.value * scaleFactor).dp.coerceIn(170.dp, 280.dp),
        displayFontScale = baseDimensions.displayFontScale * scaleFactor.coerceIn(0.8f, 1.15f)
    )

    CompositionLocalProvider(LocalAppDimensions provides scaledDimensions) {
        content()
    }
}
