package com.example.habitflow

import com.example.habitflow.data.dao.HabitDao
import com.example.habitflow.data.dao.StudySessionDao
import com.example.habitflow.data.dao.TaskDao
import com.example.habitflow.data.dao.UserProgressDao
import com.example.habitflow.data.entity.HabitEntity
import com.example.habitflow.data.entity.StudySessionEntity
import com.example.habitflow.data.entity.TaskEntity
import com.example.habitflow.data.entity.UserProgressEntity
import com.example.habitflow.data.repository.HabitRepository
import com.example.habitflow.data.repository.StudyRepository
import com.example.habitflow.data.repository.TaskRepository
import com.example.habitflow.data.repository.UserRepository
import com.example.habitflow.ui.viewmodel.StatsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsViewModelTest {

    private class FakeHabitDao(var habits: List<HabitEntity> = emptyList()) : HabitDao {
        override fun getAllHabits(): Flow<List<HabitEntity>> = flowOf(habits)
        override suspend fun getHabitById(id: Long): HabitEntity? = habits.find { it.id == id }
        override suspend fun insertHabit(habit: HabitEntity): Long = 1L
        override suspend fun insertHabits(habits: List<HabitEntity>) {}
        override suspend fun updateHabit(habit: HabitEntity) {}
        override suspend fun deleteHabit(habit: HabitEntity) {}
        override suspend fun deleteAllHabits() {}
    }

    private class FakeTaskDao(var tasks: List<TaskEntity> = emptyList()) : TaskDao {
        override fun getAllTasks(): Flow<List<TaskEntity>> = flowOf(tasks)
        override fun getTasksByDate(date: String): Flow<List<TaskEntity>> = flowOf(tasks)
        override suspend fun getTaskById(id: Long): TaskEntity? = tasks.find { it.id == id }
        override suspend fun insertTask(task: TaskEntity): Long = 1L
        override suspend fun insertTasks(tasks: List<TaskEntity>) {}
        override suspend fun updateTask(task: TaskEntity) {}
        override suspend fun deleteTask(task: TaskEntity) {}
    }

    private class FakeStudySessionDao(var sessions: List<StudySessionEntity> = emptyList()) : StudySessionDao {
        override fun getAllSessions(): Flow<List<StudySessionEntity>> = flowOf(sessions)
        override suspend fun insertSession(session: StudySessionEntity): Long = 1L
        override suspend fun deleteSession(session: StudySessionEntity) {}
    }

    private class FakeUserProgressDao(var progress: UserProgressEntity? = UserProgressEntity(id = 1L, level = 5, currentXp = 1250, maxXP = 2000, streakDays = 15, totalFocusHours = 50.0)) : UserProgressDao {
        override fun getUserProgress(): Flow<UserProgressEntity?> = flowOf(progress)
        override suspend fun getUserProgressSync(): UserProgressEntity? = progress
        override suspend fun insertUserProgress(progress: UserProgressEntity) { this.progress = progress }
        override suspend fun updateUserProgress(progress: UserProgressEntity) { this.progress = progress }
    }

    @Test
    fun testStatsViewModelComputesRoomDataCorrectly() = runBlocking {
        val habits = listOf(
            HabitEntity(id = 1, title = "Kotlin Coding", category = "Coding", xpReward = 50, dueDate = "Today", isCompleted = true),
            HabitEntity(id = 2, title = "Meditation", category = "Personal", xpReward = 30, dueDate = "Today", isCompleted = true),
            HabitEntity(id = 3, title = "Running", category = "Health", xpReward = 40, dueDate = "Today", isCompleted = false)
        )
        val tasks = listOf(
            TaskEntity(id = 1, title = "Task 1", estimatedMins = 30, isCompleted = true, date = "Today"),
            TaskEntity(id = 2, title = "Task 2", estimatedMins = 20, isCompleted = false, date = "Today")
        )
        val sessions = listOf(
            StudySessionEntity(id = 1, title = "Deep work", durationMins = 60)
        )
        val progress = UserProgressEntity(id = 1L, level = 3, currentXp = 500, maxXP = 1000, streakDays = 10, totalFocusHours = 25.0)

        val habitRepo = HabitRepository(FakeHabitDao(habits))
        val taskRepo = TaskRepository(FakeTaskDao(tasks))
        val studyRepo = StudyRepository(FakeStudySessionDao(sessions))
        val userRepo = UserRepository(FakeUserProgressDao(progress))

        val viewModel = StatsViewModel(studyRepo, taskRepo, userRepo, habitRepo)
        val state = viewModel.uiState.drop(1).first()

        assertEquals(10, state.streakDays)
        assertEquals(25.0, state.totalFocusHours, 0.001)
        assertEquals(3, state.completedTasksCount) // 1 task + 2 habits completed
        assertEquals(3, state.currentLevel)
        assertEquals(500, state.currentXp)
        assertEquals(1000, state.maxXp)
    }
}
