package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.FloatingBottomBar
import com.example.ui.components.NavDestination
import com.example.ui.components.ProvideAppDimensions
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.festivals.FestivalsScreen
import com.example.ui.screens.home.AddEditTaskDialog
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.pomodoro.PomodoroScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.theme.DaywillTheme
import com.example.ui.viewmodel.DaywillViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DaywillViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userSettings by viewModel.userSettings.collectAsState()

            DaywillTheme(
                themeName = userSettings.themeName,
                appearanceMode = userSettings.appearanceMode
            ) {
                ProvideAppDimensions(isCompactMode = userSettings.isCompactMode) {
                    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }

                    var showAddTaskDialog by remember { mutableStateOf(false) }
                    var addTaskInitialDate by remember { mutableStateOf<String?>(null) }

                    if (showAddTaskDialog) {
                        AddEditTaskDialog(
                            initialDateString = addTaskInitialDate,
                            onDismiss = {
                                showAddTaskDialog = false
                                addTaskInitialDate = null
                            },
                            onConfirm = { title, dateStr, timeStr, notes, targetPomodoros, cat ->
                                viewModel.addTask(title, dateStr, timeStr, notes, targetPomodoros, cat)
                                showAddTaskDialog = false
                                addTaskInitialDate = null
                            }
                        )
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            FloatingBottomBar(
                                currentDestination = currentDestination,
                                onDestinationSelected = { currentDestination = it }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentDestination,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "ScreenTransition"
                            ) { destination ->
                                when (destination) {
                                    NavDestination.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToPomodoro = { currentDestination = NavDestination.POMODORO },
                                        onNavigateToCalendar = { currentDestination = NavDestination.CALENDAR },
                                        onNavigateToFestivals = { currentDestination = NavDestination.FESTIVALS },
                                        onOpenAddTask = {
                                            addTaskInitialDate = null
                                            showAddTaskDialog = true
                                        }
                                    )

                                    NavDestination.POMODORO -> PomodoroScreen(
                                        viewModel = viewModel
                                    )

                                    NavDestination.CALENDAR -> CalendarScreen(
                                        viewModel = viewModel,
                                        onOpenAddTaskForDate = { dateStr ->
                                            addTaskInitialDate = dateStr
                                            showAddTaskDialog = true
                                        }
                                    )

                                    NavDestination.FESTIVALS -> FestivalsScreen(
                                        viewModel = viewModel
                                    )

                                    NavDestination.PROFILE -> ProfileScreen(
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
