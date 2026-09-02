package com.example.data

import com.example.data.dao.FestivalDao
import com.example.data.dao.SessionDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.TaskDao
import com.example.data.model.FestivalEvent
import com.example.data.model.PlannerTask
import com.example.data.model.PomodoroSession
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DaywillRepository(
    private val taskDao: TaskDao,
    private val sessionDao: SessionDao,
    private val festivalDao: FestivalDao,
    private val settingsDao: SettingsDao
) {
    val allTasks: Flow<List<PlannerTask>> = taskDao.getAllTasks()
    val allSessions: Flow<List<PomodoroSession>> = sessionDao.getAllSessions()
    val allFestivals: Flow<List<FestivalEvent>> = festivalDao.getAllFestivals()
    val userSettings: Flow<UserSettings?> = settingsDao.getSettingsFlow()

    fun getTasksForDate(date: String): Flow<List<PlannerTask>> = taskDao.getTasksForDate(date)

    suspend fun insertTask(task: PlannerTask): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: PlannerTask) = taskDao.updateTask(task)
    suspend fun deleteTask(task: PlannerTask) = taskDao.deleteTask(task)

    suspend fun insertSession(session: PomodoroSession): Long = sessionDao.insertSession(session)

    suspend fun insertFestival(festival: FestivalEvent): Long = festivalDao.insertFestival(festival)
    suspend fun updateFestival(festival: FestivalEvent) = festivalDao.updateFestival(festival)
    suspend fun deleteFestival(festival: FestivalEvent) = festivalDao.deleteFestival(festival)

    suspend fun saveSettings(settings: UserSettings) = settingsDao.saveSettings(settings)

    suspend fun seedInitialDataIfEmpty() {
        // Settings seed
        val currentSettings = settingsDao.getSettingsDirect()
        if (currentSettings == null) {
            settingsDao.saveSettings(UserSettings())
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Sample task seed if empty
        // Check festivals seed
        val sampleFestivals = listOf(
            FestivalEvent(
                name = "Dashain",
                dateString = "2026-10-15",
                category = "Cultural",
                description = "Major festival celebrating victory of good over evil, family blessings and feasts."
            ),
            FestivalEvent(
                name = "Tihar",
                dateString = "2026-10-31",
                category = "Cultural",
                description = "Festival of lights, marigold garlands, and honoring birds and animals."
            ),
            FestivalEvent(
                name = "Chhath",
                dateString = "2026-11-07",
                category = "Cultural",
                description = "Ancient Vedic sun worship festival giving thanks for life and energy."
            ),
            FestivalEvent(
                name = "Christmas",
                dateString = "2026-12-25",
                category = "National",
                description = "Annual commemoration of the birth of Jesus Christ and holiday joy."
            ),
            FestivalEvent(
                name = "New Year's Day",
                dateString = "2027-01-01",
                category = "National",
                description = "Celebrating the start of a fresh brand new year!"
            )
        )

        festivalDao.insertAllFestivals(sampleFestivals)

        // Seed sample tasks for today
        val sampleTasks = listOf(
            PlannerTask(
                title = "Deep Work: Mobile Architecture",
                dateString = todayDate,
                timeString = "10:00 AM",
                notes = "Focus on Room DB and Jetpack Compose state reflowing.",
                targetPomodoros = 2,
                completedPomodoros = 1,
                isCompleted = false,
                category = "Work"
            ),
            PlannerTask(
                title = "Festival Planning & Shopping",
                dateString = todayDate,
                timeString = "02:30 PM",
                notes = "Prepare gifts and decorations for upcoming Dashain.",
                targetPomodoros = 1,
                completedPomodoros = 0,
                isCompleted = false,
                category = "Personal"
            )
        )
        for (task in sampleTasks) {
            taskDao.insertTask(task)
        }

        // Seed sample sessions
        val sampleSessions = listOf(
            PomodoroSession(
                durationMinutes = 25,
                mode = "FOCUS",
                taskTitle = "Deep Work: Mobile Architecture"
            ),
            PomodoroSession(
                durationMinutes = 25,
                mode = "FOCUS",
                taskTitle = "Code Review"
            ),
            PomodoroSession(
                durationMinutes = 5,
                mode = "SHORT_BREAK",
                taskTitle = "Coffee Break"
            )
        )
        for (session in sampleSessions) {
            sessionDao.insertSession(session)
        }
    }

    suspend fun clearAllData() {
        taskDao.deleteAllTasks()
        sessionDao.deleteAllSessions()
        festivalDao.deleteAllFestivals()
        settingsDao.saveSettings(UserSettings())
    }
}
