package com.example.ui.screens.pomodoro

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.sound.AmbientSoundManager
import com.example.ui.sound.SoundPresets
import com.example.ui.viewmodel.DaywillViewModel
import com.example.ui.viewmodel.PomodoroMode
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PomodoroScreen(
    viewModel: DaywillViewModel,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalAppDimensions.current
    val timerState by viewModel.timerState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()

    val soundManager = remember { AmbientSoundManager.getInstance() }
    val isPlayingSound by soundManager.isPlaying.collectAsState()
    val currentSound by soundManager.currentSound.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Timer, 1: Sessions, 2: Stats
    var showCustomDurationDialog by remember { mutableStateOf(false) }

    val minutes = timerState.secondsRemaining / 60
    val seconds = timerState.secondsRemaining % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val targetProgress = if (timerState.totalSeconds > 0) {
        timerState.secondsRemaining.toFloat() / timerState.totalSeconds.toFloat()
    } else 0f

    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 300),
        label = "timerProgress"
    )

    if (showCustomDurationDialog) {
        CustomDurationDialog(
            currentFocus = userSettings.focusDurationMinutes,
            currentShortBreak = userSettings.shortBreakMinutes,
            currentLongBreak = userSettings.longBreakMinutes,
            onDismiss = { showCustomDurationDialog = false },
            onConfirm = { focus, shortB, longB ->
                viewModel.updatePomodoroDurations(focus, shortB, longB)
                showCustomDurationDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.screenPadding),
        contentPadding = PaddingValues(top = dimensions.screenPadding, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pomodoro",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { showCustomDurationDialog = true },
                    modifier = Modifier
                        .testTag("pomodoro_settings_button")
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Custom Durations",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Segmented Tabs: Timer, Sessions, Stats
        item {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Timer", "Sessions", "Stats").forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // TIMER VIEW
            item {
                // Mode Selector Chips (Focus, Short Break, Long Break)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PomodoroMode.entries.forEach { mode ->
                        val isSelected = timerState.mode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTimerMode(mode) },
                            label = {
                                Text(
                                    text = mode.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Large Circular Timer
            item {
                CircularTimer(
                    progress = progress,
                    timeDisplay = timeFormatted,
                    modeTitle = timerState.mode.title,
                    isRunning = timerState.isRunning,
                    onStartPauseClick = { viewModel.toggleStartPauseTimer() },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Quick Control Buttons (Reset & Skip)
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.resetTimer() },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("reset_timer_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset")
                    }

                    OutlinedButton(
                        onClick = { viewModel.skipSession() },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("skip_timer_button")
                    ) {
                        Icon(imageVector = Icons.Filled.SkipNext, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Skip")
                    }
                }
            }

            // Relaxing Sound Quick Control Card
            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.PEACH) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = currentSound?.icon ?: "🌧️", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Relaxing Sound • ${currentSound?.title ?: "Rainfall"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPlayingSound) "Playing ambient audio" else "Tap play to relax while focusing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { soundManager.togglePlay() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = if (isPlayingSound) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause Sound",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SoundPresets.ALL_SOUNDS.forEach { sound ->
                            val isSelected = currentSound?.id == sound.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { soundManager.selectSound(sound) },
                                label = {
                                    Text(
                                        text = "${sound.icon} ${sound.title}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Next Session Card
            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.CREAM) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "☕", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Next: Short Break (${userSettings.shortBreakMinutes} min)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Automatic transition after focus",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // SESSIONS LOG VIEW
            item {
                Text(
                    text = "Completed Focus Sessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (sessions.isEmpty()) {
                item {
                    ExpressiveCard(pastelTheme = CardPastelTheme.SURFACE) {
                        Text(
                            text = "No focus sessions recorded yet today.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(sessions.size) { index ->
                    val session = sessions[index]
                    val dateFormatted = remember(session.timestamp) {
                        SimpleDateFormat("hh:mm a, MMM d", Locale.getDefault()).format(Date(session.timestamp))
                    }
                    ExpressiveCard(pastelTheme = CardPastelTheme.SURFACE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🍅", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = session.taskTitle.ifEmpty { "Focus Session" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = dateFormatted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${session.durationMinutes} min",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else {
            // STATS VIEW
            item {
                val totalFocusMins = sessions.filter { it.mode == "FOCUS" }.sumOf { it.durationMinutes }
                val totalFocusHours = String.format(Locale.getDefault(), "%.1f", totalFocusMins / 60f)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExpressiveCard(pastelTheme = CardPastelTheme.PEACH) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Focus Time",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$totalFocusHours Hours",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(text = "🎯", fontSize = 42.sp)
                        }
                    }

                    ExpressiveCard(pastelTheme = CardPastelTheme.MINT) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sessions Completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${sessions.size} Sessions",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(text = "🔥", fontSize = 42.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDurationDialog(
    currentFocus: Int,
    currentShortBreak: Int,
    currentLongBreak: Int,
    onDismiss: () -> Unit,
    onConfirm: (focus: Int, shortBreak: Int, longBreak: Int) -> Unit
) {
    var focusMins by remember { mutableIntStateOf(currentFocus) }
    var shortMins by remember { mutableIntStateOf(currentShortBreak) }
    var longMins by remember { mutableIntStateOf(currentLongBreak) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Pomodoro Durations", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DurationSliderRow("Focus Duration", focusMins, 5..60) { focusMins = it }
                DurationSliderRow("Short Break", shortMins, 1..30) { shortMins = it }
                DurationSliderRow("Long Break", longMins, 5..45) { longMins = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(focusMins, shortMins, longMins) },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DurationSliderRow(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = "$value min", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat()
        )
    }
}
