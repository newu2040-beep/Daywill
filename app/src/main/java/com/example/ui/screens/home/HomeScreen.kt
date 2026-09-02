package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FestivalEvent
import com.example.data.model.PlannerTask
import com.example.ui.components.*
import com.example.ui.sound.AmbientSoundManager
import com.example.ui.sound.SoundPresets
import com.example.ui.theme.*
import com.example.ui.viewmodel.DaywillViewModel
import com.example.ui.viewmodel.PomodoroMode
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: DaywillViewModel,
    onNavigateToPomodoro: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToFestivals: () -> Unit,
    onOpenAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalAppDimensions.current
    val timerState by viewModel.timerState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val festivals by viewModel.allFestivals.collectAsState()

    val soundManager = remember { AmbientSoundManager.getInstance() }
    val isPlayingSound by soundManager.isPlaying.collectAsState()
    val currentSound by soundManager.currentSound.collectAsState()

    val todayString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val todayTasks = tasks.filter { it.dateString == todayString }
    val nextFestival = festivals.firstOrNull { it.dateString >= todayString } ?: festivals.firstOrNull()

    // Time formatting helper
    val minutes = timerState.secondsRemaining / 60
    val seconds = timerState.secondsRemaining % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.screenPadding),
        contentPadding = PaddingValues(top = dimensions.screenPadding, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        // 1. Greeting & Date Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning,",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${userSettings.userName}! ☀️",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = (MaterialTheme.typography.headlineLarge.fontSize.value * dimensions.displayFontScale).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Quick appearance toggle
                IconButton(
                    onClick = {
                        val nextMode = when (userSettings.appearanceMode) {
                            "Light" -> "Dark"
                            "Dark" -> "Light"
                            else -> "Dark"
                        }
                        viewModel.updateAppearanceMode(nextMode)
                    },
                    modifier = Modifier
                        .testTag("theme_toggle_button")
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (userSettings.appearanceMode == "Dark") Icons.Filled.DarkMode else Icons.Filled.WbSunny,
                        contentDescription = "Toggle Appearance",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 2. Ambient Sound Quick Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.cornerRadiusMedium)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.cardPadding, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = currentSound?.icon ?: "🌧️",
                            fontSize = 20.sp
                        )
                        Column {
                            Text(
                                text = "Relaxing Sound • ${currentSound?.title ?: "Rainfall"}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPlayingSound) "Playing ambient audio" else "Tap play to relax while focusing",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { soundManager.togglePlay() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (isPlayingSound) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause Sound",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 3. Prominent Expressive Pomodoro Focus Card
        item {
            ExpressiveCard(
                pastelTheme = CardPastelTheme.PEACH,
                showOrganicLeaf = true,
                onClick = onNavigateToPomodoro,
                modifier = Modifier.testTag("home_pomodoro_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🍅", fontSize = if (dimensions.screenPadding < 12.dp) 32.sp else 42.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = timerState.mode.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PastelPeachAccent
                            )
                            Text(
                                text = if (timerState.isRunning) "In Progress" else "Focus time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = (MaterialTheme.typography.headlineLarge.fontSize.value * dimensions.displayFontScale).sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { viewModel.toggleStartPauseTimer() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PastelPeachAccent)
                                .size(if (dimensions.screenPadding < 12.dp) 38.dp else 44.dp)
                        ) {
                            Icon(
                                imageVector = if (timerState.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Start/Pause",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 4. Stat Summary Pill Row (Sessions / Festivals / Streak)
        item {
            val isCompact = dimensions.screenPadding < 12.dp

            if (isCompact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPill(
                            icon = Icons.Filled.CheckCircle,
                            title = "${timerState.completedSessionsToday}/5",
                            subtitle = "Today's Sessions",
                            bgColor = PastelGreen,
                            accentColor = PastelGreenAccent,
                            modifier = Modifier.weight(1f)
                        )
                        StatPill(
                            icon = Icons.Filled.CalendarMonth,
                            title = "${festivals.size}",
                            subtitle = "Festivals",
                            bgColor = PastelMint,
                            accentColor = TealPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    StatPill(
                        icon = Icons.Filled.LocalFireDepartment,
                        title = "${userSettings.currentStreak} Days",
                        subtitle = "Day Streak",
                        bgColor = PastelYellow,
                        accentColor = PastelYellowAccent,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatPill(
                        icon = Icons.Filled.CheckCircle,
                        title = "${timerState.completedSessionsToday}/5",
                        subtitle = "Sessions",
                        bgColor = PastelGreen,
                        accentColor = PastelGreenAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        icon = Icons.Filled.CalendarMonth,
                        title = "${festivals.size}",
                        subtitle = "Festivals",
                        bgColor = PastelMint,
                        accentColor = TealPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        icon = Icons.Filled.LocalFireDepartment,
                        title = "${userSettings.currentStreak}",
                        subtitle = "Streak",
                        bgColor = PastelYellow,
                        accentColor = PastelYellowAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Today's Focus / Daily Planner Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Focus",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onOpenAddTask,
                    modifier = Modifier.testTag("view_plan_button")
                ) {
                    Text(
                        text = "+ Add Task",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.MINT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Task,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "No tasks planned for today",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap '+ Add Task' to set your focus goals.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(todayTasks.size) { index ->
                val task = todayTasks[index]
                TaskItemCard(
                    task = task,
                    onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                    onStartPomodoro = {
                        viewModel.setTimerMode(PomodoroMode.FOCUS, task)
                        onNavigateToPomodoro()
                    }
                )
            }
        }

        // 6. Today's Festivals / Special Days Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Festival",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onNavigateToCalendar,
                    modifier = Modifier.testTag("view_calendar_button")
                ) {
                    Text(
                        text = "View Calendar",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            if (nextFestival != null) {
                ExpressiveCard(
                    pastelTheme = CardPastelTheme.YELLOW,
                    onClick = onNavigateToFestivals,
                    modifier = Modifier.testTag("home_festival_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛕", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = nextFestival.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelYellowAccent
                                )
                                Text(
                                    text = "${nextFestival.dateString} • ${nextFestival.category}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = PastelYellowAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Upcoming",
                                style = MaterialTheme.typography.labelLarge,
                                color = PastelYellowAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. Motivational Banner Card
        item {
            ExpressiveCard(pastelTheme = CardPastelTheme.MINT) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💡", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "\"Small focus sessions create big results.\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    bgColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val actualBgColor = if (isDark) accentColor.copy(alpha = 0.18f) else bgColor

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = actualBgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MaterialTheme.colorScheme.onSurface else accentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
