package com.example.data.dao

import androidx.room.*
import com.example.data.model.FestivalEvent
import com.example.data.model.PlannerTask
import com.example.data.model.PomodoroSession
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM planner_tasks ORDER BY dateString ASC, id DESC")
    fun getAllTasks(): Flow<List<PlannerTask>>

    @Query("SELECT * FROM planner_tasks WHERE dateString = :date ORDER BY id DESC")
    fun getTasksForDate(date: String): Flow<List<PlannerTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PlannerTask): Long

    @Update
    suspend fun updateTask(task: PlannerTask)

    @Delete
    suspend fun deleteTask(task: PlannerTask)

    @Query("DELETE FROM planner_tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSession): Long

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAllSessions()
}

@Dao
interface FestivalDao {
    @Query("SELECT * FROM festival_events ORDER BY dateString ASC")
    fun getAllFestivals(): Flow<List<FestivalEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFestival(festival: FestivalEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFestivals(festivals: List<FestivalEvent>)

    @Update
    suspend fun updateFestival(festival: FestivalEvent)

    @Delete
    suspend fun deleteFestival(festival: FestivalEvent)

    @Query("DELETE FROM festival_events")
    suspend fun deleteAllFestivals()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettingsDirect(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettings)
}
