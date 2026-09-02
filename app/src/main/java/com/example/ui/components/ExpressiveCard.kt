package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

enum class CardPastelTheme {
    PEACH, LAVENDER, MINT, YELLOW, CREAM, SURFACE
}

@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    pastelTheme: CardPastelTheme = CardPastelTheme.SURFACE,
    showOrganicLeaf: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = LocalAppDimensions.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val (bgColor, contentColor) = when (pastelTheme) {
        CardPastelTheme.PEACH -> if (isDark) {
            Pair(Color(0xFF381E1A), Color(0xFFFFDAD4))
        } else {
            Pair(Color(0xFFFFEDEA), Color(0xFF5E1B10))
        }
        CardPastelTheme.LAVENDER -> if (isDark) {
            Pair(Color(0xFF2E2140), Color(0xFFE8DDFF))
        } else {
            Pair(Color(0xFFF1EDF9), Color(0xFF2C194D))
        }
        CardPastelTheme.MINT -> if (isDark) {
            Pair(Color(0xFF13322B), Color(0xFFC4EEDF))
        } else {
            Pair(Color(0xFFE8F8F5), Color(0xFF0C3D32))
        }
        CardPastelTheme.YELLOW -> if (isDark) {
            Pair(Color(0xFF362B16), Color(0xFFFFDEA8))
        } else {
            Pair(Color(0xFFFFF8E7), Color(0xFF422C00))
        }
        CardPastelTheme.CREAM -> if (isDark) {
            Pair(Color(0xFF223530), Color(0xFFE0E3E1))
        } else {
            Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        }
        CardPastelTheme.SURFACE -> Pair(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface
        )
    }

    val cardShape = RoundedCornerShape(dimensions.cornerRadiusLarge)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 2.dp else 6.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.3f else 0.04f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.06f)
            )
            .clip(cardShape),
        color = bgColor,
        contentColor = contentColor,
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (showOrganicLeaf) {
                // Subtle organic background leaf accent
                Canvas(
                    modifier = Modifier.matchParentSize()
                ) {
                    val leafPath = Path().apply {
                        moveTo(size.width * 0.75f, 0f)
                        cubicTo(
                            size.width * 0.95f, size.height * 0.2f,
                            size.width, size.height * 0.7f,
                            size.width * 0.7f, size.height
                        )
                        cubicTo(
                            size.width * 0.5f, size.height * 0.6f,
                            size.width * 0.6f, size.height * 0.1f,
                            size.width * 0.75f, 0f
                        )
                        close()
                    }
                    drawPath(
                        path = leafPath,
                        color = contentColor.copy(alpha = if (isDark) 0.12f else 0.06f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensions.cardPadding)
            ) {
                content()
            }
        }
    }
}
