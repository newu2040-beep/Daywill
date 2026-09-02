package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FestivalDao
import com.example.data.dao.SessionDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.TaskDao
import com.example.data.model.FestivalEvent
import com.example.data.model.PlannerTask
import com.example.data.model.PomodoroSession
import com.example.data.model.UserSettings

@Database(
    entities = [PlannerTask::class, PomodoroSession::class, FestivalEvent::class, UserSettings::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun sessionDao(): SessionDao
    abstract fun festivalDao(): FestivalDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daywill_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
