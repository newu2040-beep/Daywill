package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary

data class BarChartItem(
    val label: String, // e.g. "Mon"
    val valueMinutes: Int // e.g. 120
)

@Composable
fun ExpressiveBarChart(
    items: List<BarChartItem>,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalAppDimensions.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    val maxVal = (items.maxOfOrNull { it.valueMinutes } ?: 1).coerceAtLeast(60)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (dimensions.screenPadding < 12.dp) 130.dp else 170.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = (size.width / (items.size * 2f))
                val spacing = barWidth

                items.forEachIndexed { index, item ->
                    val x = spacing / 2 + index * (barWidth + spacing)
                    val barHeight = (item.valueMinutes.toFloat() / maxVal.toFloat()) * (size.height - 24.dp.toPx())

                    // Background track bar
                    drawRoundRect(
                        color = containerColor,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, size.height - 24.dp.toPx()),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )

                    // Value filled bar
                    if (barHeight > 0) {
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(x, size.height - 24.dp.toPx() - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}
