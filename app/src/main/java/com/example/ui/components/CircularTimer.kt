package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PastelPeachAccent
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary

@Composable
fun CircularTimer(
    progress: Float, // 0.0f to 1.0f
    timeDisplay: String, // e.g. "25:00"
    modeTitle: String, // e.g. "Focus Time"
    isRunning: Boolean,
    onStartPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalAppDimensions.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "timerProgress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .size(dimensions.timerSize)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Circular progress ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Background track
            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Animated progress arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cute tomato icon/graphic
            Text(
                text = if (modeTitle.contains("Break", ignoreCase = true)) "☕" else "🍅",
                fontSize = if (dimensions.screenPadding < 12.dp) 32.sp else 44.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Time text
            Text(
                text = timeDisplay,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = (MaterialTheme.typography.displayLarge.fontSize.value * dimensions.displayFontScale).sp
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = modeTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Play / Pause FAB
            FloatingActionButton(
                onClick = onStartPauseClick,
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .testTag("timer_start_pause_button")
                    .size(if (dimensions.screenPadding < 12.dp) 48.dp else 58.dp)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isRunning) "Pause Timer" else "Start Timer",
                    modifier = Modifier.size(if (dimensions.screenPadding < 12.dp) 24.dp else 30.dp)
                )
            }
        }
    }
}
