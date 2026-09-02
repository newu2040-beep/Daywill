package com.example.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FestivalEvent
import com.example.data.model.PlannerTask
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DaywillViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: DaywillViewModel,
    onOpenAddTaskForDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalAppDimensions.current
    val tasks by viewModel.allTasks.collectAsState()
    val festivals by viewModel.allFestivals.collectAsState()

    val calendar = remember { Calendar.getInstance() }
    var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) } // 0-indexed

    val todayFormatted = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    var selectedDateString by remember { mutableStateOf(todayFormatted) }

    val monthName = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    val daysInMonth = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sun
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Pair(firstDayOfWeek, totalDays)
    }

    val selectedDateTasks = tasks.filter { it.dateString == selectedDateString }
    val selectedDateFestival = festivals.find { it.dateString == selectedDateString }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.screenPadding),
        contentPadding = PaddingValues(top = dimensions.screenPadding, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        // Month / Year Navigation Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    },
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "Previous Month")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val now = Calendar.getInstance()
                            currentYear = now.get(Calendar.YEAR)
                            currentMonth = now.get(Calendar.MONTH)
                            selectedDateString = todayFormatted
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("today_calendar_button")
                    ) {
                        Text("Today", fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    },
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Next Month")
                }
            }
        }

        // Calendar Day Grid Box
        item {
            Surface(
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Day of week headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Month Days Grid
                    val (offset, totalDays) = daysInMonth
                    val totalGridCells = offset + totalDays
                    val rows = (totalGridCells + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (c in 0 until 7) {
                                    val dayNum = r * 7 + c - offset + 1
                                    if (dayNum in 1..totalDays) {
                                        val dateStr = String.format(
                                            Locale.getDefault(),
                                            "%04d-%02d-%02d",
                                            currentYear,
                                            currentMonth + 1,
                                            dayNum
                                        )
                                        val isSelected = dateStr == selectedDateString
                                        val isToday = dateStr == todayFormatted

                                        val hasTask = tasks.any { it.dateString == dateStr }
                                        val hasFestival = festivals.any { it.dateString == dateStr }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .testTag("calendar_day_$dateStr")
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSelected -> MaterialTheme.colorScheme.primary
                                                        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(
                                                            alpha = 0.5f
                                                        )

                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable { selectedDateString = dateStr },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isToday -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )

                                                // Event Indicators (Dots)
                                                if (hasTask || hasFestival) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        if (hasFestival) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else PastelYellowAccent)
                                                            )
                                                        }
                                                        if (hasTask) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else TealPrimary)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Detail Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected Date: $selectedDateString",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = { onOpenAddTaskForDate(selectedDateString) },
                    modifier = Modifier.testTag("add_event_for_date_button")
                ) {
                    Text("+ Add Event")
                }
            }
        }

        // Selected Date Festival Banner if exists
        item {
            if (selectedDateFestival != null) {
                ExpressiveCard(pastelTheme = CardPastelTheme.YELLOW) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛕", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = selectedDateFestival.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelYellowAccent
                                )
                                Text(
                                    text = selectedDateFestival.description.ifEmpty { "Festival Special Day" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Tasks List
        if (selectedDateTasks.isEmpty()) {
            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.SURFACE) {
                    Text(
                        text = "No planned focus events on this date.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(selectedDateTasks.size) { index ->
                val task = selectedDateTasks[index]
                TaskItemCard(
                    task = task,
                    onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                    onStartPomodoro = {}
                )
            }
        }

        // Upcoming Festivals List Section
        item {
            Text(
                text = "Upcoming Festivals",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        items(festivals.size) { index ->
            val festival = festivals[index]
            ExpressiveCard(pastelTheme = CardPastelTheme.CREAM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎉", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = festival.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = festival.dateString,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = festival.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
