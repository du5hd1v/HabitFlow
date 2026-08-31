package com.example.habitflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habitflow.data.dao.HabitDao
import com.example.habitflow.data.dao.StudySessionDao
import com.example.habitflow.data.dao.TaskDao
import com.example.habitflow.data.dao.UserProgressDao
import com.example.habitflow.data.entity.HabitEntity
import com.example.habitflow.data.entity.StudySessionEntity
import com.example.habitflow.data.entity.TaskEntity
import com.example.habitflow.data.entity.UserProgressEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HabitEntity::class,
        TaskEntity::class,
        UserProgressEntity::class,
        StudySessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun taskDao(): TaskDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitflow_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(database: AppDatabase) {
            val habitDao = database.habitDao()
            val taskDao = database.taskDao()
            val userProgressDao = database.userProgressDao()
            val studyDao = database.studySessionDao()

            // Seed User Progress matching UI requirements: Level 5 Scholar, 1,250/2,000 XP, 12 Days Streak, 42.5h focus
            userProgressDao.insertUserProgress(
                UserProgressEntity(
                    id = 1L,
                    level = 5,
                    currentXp = 1250,
                    maxXP = 2000,
                    streakDays = 12,
                    totalFocusHours = 42.5
                )
            )

            // Seed Habits matching UI requirements: MADD Kotlin Assignment +50 XP, 30 Mins Daily Reading +30 XP, Morning Exercise +40 XP
            habitDao.insertHabits(
                listOf(
                    HabitEntity(
                        title = "MADD Kotlin Assignment",
                        category = "Study",
                        xpReward = 50,
                        dueDate = "Today",
                        isCompleted = false,
                        details = "Complete Jetpack Compose modules and Room database setup."
                    ),
                    HabitEntity(
                        title = "30 Mins Daily Reading",
                        category = "Growth",
                        xpReward = 30,
                        dueDate = "Today",
                        isCompleted = true,
                        details = "Read Android Architecture documentation and clean code principles."
                    ),
                    HabitEntity(
                        title = "Morning Exercise",
                        category = "Health",
                        xpReward = 40,
                        dueDate = "Today",
                        isCompleted = false,
                        details = "Cardio workout and stretching session."
                    )
                )
            )

            // Seed initial tasks
            taskDao.insertTasks(
                listOf(
                    TaskEntity(
                        title = "Review Architecture components",
                        estimatedMins = 25,
                        isCompleted = true,
                        date = "Today"
                    ),
                    TaskEntity(
                        title = "Implement Room DAOs & Repositories",
                        estimatedMins = 45,
                        isCompleted = false,
                        date = "Today"
                    )
                )
            )

            // Seed study sessions
            studyDao.insertSession(
                StudySessionEntity(
                    title = "Kotlin Flow Deep Dive",
                    durationMins = 45,
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            )
        }
    }
}
