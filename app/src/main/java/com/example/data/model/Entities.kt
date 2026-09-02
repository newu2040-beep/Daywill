package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planner_tasks")
data class PlannerTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateString: String, // YYYY-MM-DD
    val timeString: String = "09:00",
    val notes: String = "",
    val targetPomodoros: Int = 2,
    val completedPomodoros: Int = 0,
    val isCompleted: Boolean = false,
    val category: String = "Work"
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val mode: String, // "FOCUS", "SHORT_BREAK", "LONG_BREAK"
    val linkedTaskId: Long? = null,
    val taskTitle: String = ""
)

@Entity(tableName = "festival_events")
data class FestivalEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateString: String, // YYYY-MM-DD
    val category: String, // "Cultural", "National", "Personal", "Custom"
    val description: String = "",
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val themeName: String = "Classic Teal", // Classic Teal, Mint, Lavender, Peach, Ocean, Sunset, Forest, Dynamic
    val appearanceMode: String = "System", // System, Light, Dark
    val isCompactMode: Boolean = false,
    val focusDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val autoStartBreaks: Boolean = false,
    val autoStartFocus: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val currentStreak: Int = 7,
    val bestStreak: Int = 14,
    val userName: String = "Rahul Shah",
    val userEmail: String = "rahul.daywill@example.com",
    val userBio: String = "Building daily focus habits and tracking productivity goals.",
    val userAvatarUri: String = "",
    val targetDailyFocusHours: Int = 4,
    val region: String = "India / Global",
    val notificationEnabled: Boolean = true,
    val galleryPermissionGranted: Boolean = true
)
