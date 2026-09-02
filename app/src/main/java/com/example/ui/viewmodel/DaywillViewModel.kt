package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DaywillRepository
import com.example.data.model.FestivalEvent
import com.example.data.model.PlannerTask
import com.example.data.model.PomodoroSession
import com.example.data.model.UserSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PomodoroMode(val title: String) {
    FOCUS("Focus Time"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break")
}

data class TimerState(
    val mode: PomodoroMode = PomodoroMode.FOCUS,
    val totalSeconds: Int = 25 * 60,
    val secondsRemaining: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedSessionsToday: Int = 3,
    val linkedTask: PlannerTask? = null
)

class DaywillViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DaywillRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DaywillRepository(db.taskDao(), db.sessionDao(), db.festivalDao(), db.settingsDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .map { it ?: UserSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val allTasks: StateFlow<List<PlannerTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<PomodoroSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFestivals: StateFlow<List<FestivalEvent>> = repository.allFestivals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pomodoro Timer State
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Sync timer initial duration from settings
        viewModelScope.launch {
            userSettings.collect { settings ->
                if (!_timerState.value.isRunning) {
                    val defaultSecs = when (_timerState.value.mode) {
                        PomodoroMode.FOCUS -> settings.focusDurationMinutes * 60
                        PomodoroMode.SHORT_BREAK -> settings.shortBreakMinutes * 60
                        PomodoroMode.LONG_BREAK -> settings.longBreakMinutes * 60
                    }
                    _timerState.value = _timerState.value.copy(
                        totalSeconds = defaultSecs,
                        secondsRemaining = defaultSecs
                    )
                }
            }
        }
    }

    fun setTimerMode(mode: PomodoroMode, task: PlannerTask? = null) {
        timerJob?.cancel()
        val settings = userSettings.value
        val durationMinutes = when (mode) {
            PomodoroMode.FOCUS -> settings.focusDurationMinutes
            PomodoroMode.SHORT_BREAK -> settings.shortBreakMinutes
            PomodoroMode.LONG_BREAK -> settings.longBreakMinutes
        }
        val totalSecs = durationMinutes * 60
        _timerState.value = TimerState(
            mode = mode,
            totalSeconds = totalSecs,
            secondsRemaining = totalSecs,
            isRunning = false,
            completedSessionsToday = _timerState.value.completedSessionsToday,
            linkedTask = task ?: _timerState.value.linkedTask
        )
    }

    fun toggleStartPauseTimer() {
        if (_timerState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (_timerState.value.secondsRemaining <= 0) return
        _timerState.value = _timerState.value.copy(isRunning = true)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.secondsRemaining > 0 && _timerState.value.isRunning) {
                delay(1000L)
                val remaining = _timerState.value.secondsRemaining - 1
                _timerState.value = _timerState.value.copy(secondsRemaining = remaining)
            }

            if (_timerState.value.secondsRemaining == 0) {
                onTimerFinished()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        val totalSecs = _timerState.value.totalSeconds
        _timerState.value = _timerState.value.copy(
            secondsRemaining = totalSecs,
            isRunning = false
        )
    }

    fun skipSession() {
        timerJob?.cancel()
        val nextMode = when (_timerState.value.mode) {
            PomodoroMode.FOCUS -> PomodoroMode.SHORT_BREAK
            PomodoroMode.SHORT_BREAK -> PomodoroMode.FOCUS
            PomodoroMode.LONG_BREAK -> PomodoroMode.FOCUS
        }
        setTimerMode(nextMode)
    }

    private fun onTimerFinished() {
        _timerState.value = _timerState.value.copy(isRunning = false)
        triggerVibration()

        val settings = userSettings.value
        val context = getApplication<Application>().applicationContext

        if (_timerState.value.mode == PomodoroMode.FOCUS) {
            val completedCount = _timerState.value.completedSessionsToday + 1
            _timerState.value = _timerState.value.copy(completedSessionsToday = completedCount)

            // Log session in Room DB
            viewModelScope.launch {
                repository.insertSession(
                    PomodoroSession(
                        durationMinutes = _timerState.value.totalSeconds / 60,
                        mode = "FOCUS",
                        linkedTaskId = _timerState.value.linkedTask?.id,
                        taskTitle = _timerState.value.linkedTask?.title ?: "Focus Session"
                    )
                )

                _timerState.value.linkedTask?.let { task ->
                    val updatedTask = task.copy(
                        completedPomodoros = task.completedPomodoros + 1,
                        isCompleted = task.completedPomodoros + 1 >= task.targetPomodoros
                    )
                    repository.updateTask(updatedTask)
                }
            }

            Toast.makeText(context, "🎉 Focus session complete! Take a break.", Toast.LENGTH_LONG).show()

            if (settings.autoStartBreaks) {
                setTimerMode(PomodoroMode.SHORT_BREAK)
                startTimer()
            } else {
                setTimerMode(PomodoroMode.SHORT_BREAK)
            }
        } else {
            Toast.makeText(context, "⏰ Break finished! Ready to focus again?", Toast.LENGTH_LONG).show()

            if (settings.autoStartFocus) {
                setTimerMode(PomodoroMode.FOCUS)
                startTimer()
            } else {
                setTimerMode(PomodoroMode.FOCUS)
            }
        }
    }

    private fun triggerVibration() {
        if (!userSettings.value.vibrationEnabled) return
        val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }

    // Task Operations
    fun addTask(title: String, dateString: String, timeString: String, notes: String, targetPomodoros: Int, category: String) {
        viewModelScope.launch {
            repository.insertTask(
                PlannerTask(
                    title = title,
                    dateString = dateString,
                    timeString = timeString,
                    notes = notes,
                    targetPomodoros = targetPomodoros,
                    category = category
                )
            )
        }
    }

    fun toggleTaskCompletion(task: PlannerTask) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: PlannerTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Festival Operations
    fun addCustomFestival(name: String, dateString: String, category: String, description: String) {
        viewModelScope.launch {
            repository.insertFestival(
                FestivalEvent(
                    name = name,
                    dateString = dateString,
                    category = category,
                    description = description,
                    isCustom = true,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleFestivalEnabled(festival: FestivalEvent) {
        viewModelScope.launch {
            repository.updateFestival(festival.copy(isEnabled = !festival.isEnabled))
        }
    }

    fun deleteFestival(festival: FestivalEvent) {
        viewModelScope.launch {
            repository.deleteFestival(festival)
        }
    }

    fun updateUserProfile(
        name: String,
        email: String,
        bio: String,
        avatarUri: String,
        dailyTargetHours: Int,
        region: String
    ) {
        viewModelScope.launch {
            val updated = userSettings.value.copy(
                userName = name,
                userEmail = email,
                userBio = bio,
                userAvatarUri = avatarUri,
                targetDailyFocusHours = dailyTargetHours,
                region = region
            )
            repository.saveSettings(updated)
            Toast.makeText(getApplication(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleNotificationPermission(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSettings(userSettings.value.copy(notificationEnabled = enabled))
        }
    }

    fun toggleGalleryPermission(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSettings(userSettings.value.copy(galleryPermissionGranted = enabled))
        }
    }

    // Settings Updates
    fun updateTheme(themeName: String) {
        viewModelScope.launch {
            repository.saveSettings(userSettings.value.copy(themeName = themeName))
        }
    }

    fun updateAppearanceMode(mode: String) { // "System", "Light", "Dark"
        viewModelScope.launch {
            repository.saveSettings(userSettings.value.copy(appearanceMode = mode))
        }
    }

    fun toggleCompactMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSettings(userSettings.value.copy(isCompactMode = enabled))
        }
    }

    fun updatePomodoroDurations(focusMins: Int, shortBreakMins: Int, longBreakMins: Int) {
        viewModelScope.launch {
            val updated = userSettings.value.copy(
                focusDurationMinutes = focusMins,
                shortBreakMinutes = shortBreakMins,
                longBreakMinutes = longBreakMins
            )
            repository.saveSettings(updated)
            setTimerMode(_timerState.value.mode)
        }
    }

    fun toggleAutoStartBreaks(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSettings(userSettings.value.copy(autoStartBreaks = enabled))
        }
    }

    fun toggleAutoStartFocus(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSettings(userSettings.value.copy(autoStartFocus = enabled))
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.seedInitialDataIfEmpty()
            Toast.makeText(getApplication(), "Data reset to default successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
