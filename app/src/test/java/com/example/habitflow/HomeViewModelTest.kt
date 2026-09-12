package com.example.habitflow

import com.example.habitflow.data.dao.HabitDao
import com.example.habitflow.data.dao.UserProgressDao
import com.example.habitflow.data.entity.HabitEntity
import com.example.habitflow.data.entity.UserProgressEntity
import com.example.habitflow.data.repository.HabitRepository
import com.example.habitflow.data.repository.UserRepository
import com.example.habitflow.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {

    private class FakeHabitDao : HabitDao {
        var habits = mutableListOf<HabitEntity>()
        override fun getAllHabits(): Flow<List<HabitEntity>> = flowOf(habits)
        override suspend fun getHabitById(id: Long): HabitEntity? = habits.find { it.id == id }
        override suspend fun insertHabit(habit: HabitEntity): Long {
            habits.add(habit)
            return habits.size.toLong()
        }
        override suspend fun insertHabits(habits: List<HabitEntity>) {
            this.habits.addAll(habits)
        }
        override suspend fun updateHabit(habit: HabitEntity) {
            val index = habits.indexOfFirst { it.id == habit.id }
            if (index != -1) habits[index] = habit
        }
        override suspend fun deleteHabit(habit: HabitEntity) {
            habits.removeIf { it.id == habit.id }
        }
        override suspend fun deleteAllHabits() { habits.clear() }
    }

    private class FakeUserProgressDao : UserProgressDao {
        var currentProgress = UserProgressEntity(id = 1L, level = 1, currentXp = 0, maxXP = 100, streakDays = 5, totalFocusHours = 10.0)
        override fun getUserProgress(): Flow<UserProgressEntity?> = flowOf(currentProgress)
        override suspend fun getUserProgressSync(): UserProgressEntity? = currentProgress
        override suspend fun insertUserProgress(progress: UserProgressEntity) { currentProgress = progress }
        override suspend fun updateUserProgress(progress: UserProgressEntity) { currentProgress = progress }
    }

    @Test
    fun testCalculateXpForCategory() {
        val habitRepo = HabitRepository(FakeHabitDao())
        val userRepo = UserRepository(FakeUserProgressDao())

        val viewModel = HomeViewModel(habitRepo, userRepo)

        assertEquals(50, viewModel.calculateXpForCategory("Coding"))
        assertEquals(50, viewModel.calculateXpForCategory("CODING"))
        assertEquals(45, viewModel.calculateXpForCategory("Study"))
        assertEquals(40, viewModel.calculateXpForCategory("Health"))
        assertEquals(40, viewModel.calculateXpForCategory("Fitness"))
        assertEquals(35, viewModel.calculateXpForCategory("Work"))
        assertEquals(30, viewModel.calculateXpForCategory("Personal"))
        assertEquals(25, viewModel.calculateXpForCategory("Unknown"))
        assertEquals(25, viewModel.calculateXpForCategory(""))
    }

    @Test
    fun testUserRepositoryAddXpAndFocusLevelUp() = kotlinx.coroutines.runBlocking {
        val fakeDao = FakeUserProgressDao()
        val userRepo = UserRepository(fakeDao)

        // Initial: level 1, 0/100 XP
        userRepo.addXpAndFocus(150, 1.5)

        val updated = fakeDao.getUserProgressSync()
        // 150 XP >= 100 maxXP -> level 2, xp = 50, maxXp = 120, totalFocusHours = 11.5
        assertEquals(2, updated?.level)
        assertEquals(50, updated?.currentXp)
        assertEquals(120, updated?.maxXP)
        assertEquals(11.5, updated?.totalFocusHours ?: 0.0, 0.001)
    }

    @Test
    fun testToggleHabitCompletionUpdatesXp() = kotlinx.coroutines.runBlocking {
        val fakeHabitDao = FakeHabitDao()
        val fakeUserDao = FakeUserProgressDao()
        val habitRepo = HabitRepository(fakeHabitDao)
        val userRepo = UserRepository(fakeUserDao)

        val habit = HabitEntity(id = 1L, title = "Test Habit", category = "Study", xpReward = 40, dueDate = "Today", isCompleted = false)
        fakeHabitDao.insertHabit(habit)

        val viewModel = HomeViewModel(habitRepo, userRepo)

        // Toggle to completed -> adds 40 XP
        viewModel.toggleHabitCompletion(habit)
        kotlinx.coroutines.delay(100)

        var progress = fakeUserDao.getUserProgressSync()
        assertEquals(40, progress?.currentXp)

        // Toggle back to uncompleted -> subtracts 40 XP
        val completedHabit = fakeHabitDao.getHabitById(1L)!!
        viewModel.toggleHabitCompletion(completedHabit)
        kotlinx.coroutines.delay(100)

        progress = fakeUserDao.getUserProgressSync()
        assertEquals(0, progress?.currentXp)
    }

    @Test
    fun testToggleHabitCompletionUpdatesXpFromDefaultState() = kotlinx.coroutines.runBlocking {
        val fakeHabitDao = FakeHabitDao()
        val fakeUserDao = FakeUserProgressDao().apply {
            currentProgress = UserProgressEntity(id = 1L, level = 5, currentXp = 1250, maxXP = 2000, streakDays = 12, totalFocusHours = 42.5)
        }
        val habitRepo = HabitRepository(fakeHabitDao)
        val userRepo = UserRepository(fakeUserDao)

        val habit = HabitEntity(id = 1L, title = "MADD Kotlin Assignment", category = "Study", xpReward = 50, dueDate = "Today", isCompleted = false)
        fakeHabitDao.insertHabit(habit)

        val viewModel = HomeViewModel(habitRepo, userRepo)

        // Toggle to completed -> adds 50 XP (1250 -> 1300)
        viewModel.toggleHabitCompletion(habit)
        kotlinx.coroutines.delay(100)

        var progress = fakeUserDao.getUserProgressSync()
        assertEquals(1300, progress?.currentXp)
        assertEquals(5, progress?.level)

        // Toggle back to uncompleted -> subtracts 50 XP (1300 -> 1250)
        val completedHabit = fakeHabitDao.getHabitById(1L)!!
        viewModel.toggleHabitCompletion(completedHabit)
        kotlinx.coroutines.delay(100)

        progress = fakeUserDao.getUserProgressSync()
        assertEquals(1250, progress?.currentXp)
        assertEquals(5, progress?.level)
    }

    @Test
    fun testToggleHabitCompletionUpdatesStreak() = kotlinx.coroutines.runBlocking {
        val fakeHabitDao = FakeHabitDao()
        val fakeUserDao = FakeUserProgressDao().apply {
            currentProgress = UserProgressEntity(id = 1L, level = 5, currentXp = 1250, maxXP = 2000, streakDays = 12, totalFocusHours = 42.5)
        }
        val habitRepo = HabitRepository(fakeHabitDao)
        val userRepo = UserRepository(fakeUserDao)

        val habit = HabitEntity(id = 1L, title = "Daily Habit", category = "Coding", xpReward = 50, dueDate = "Today", isCompleted = false)
        fakeHabitDao.insertHabit(habit)

        val viewModel = HomeViewModel(habitRepo, userRepo)

        // Toggle to completed -> increments streak (12 -> 13)
        viewModel.toggleHabitCompletion(habit)
        kotlinx.coroutines.delay(100)

        var progress = fakeUserDao.getUserProgressSync()
        assertEquals(13, progress?.streakDays)
        assertEquals(1300, progress?.currentXp)

        // Toggle back to uncompleted -> decrements streak (13 -> 12)
        val completedHabit = fakeHabitDao.getHabitById(1L)!!
        viewModel.toggleHabitCompletion(completedHabit)
        kotlinx.coroutines.delay(100)

        progress = fakeUserDao.getUserProgressSync()
        assertEquals(12, progress?.streakDays)
        assertEquals(1250, progress?.currentXp)
    }

    @Test
    fun testTaskCompletionUpdatesStreakAndXp() = kotlinx.coroutines.runBlocking {
        val fakeUserDao = FakeUserProgressDao().apply {
            currentProgress = UserProgressEntity(id = 1L, level = 5, currentXp = 1250, maxXP = 2000, streakDays = 12, totalFocusHours = 42.5)
        }
        val userRepo = UserRepository(fakeUserDao)
        val taskDao = object : com.example.habitflow.data.dao.TaskDao {
            var tasks = mutableListOf<com.example.habitflow.data.entity.TaskEntity>()
            override fun getAllTasks() = flowOf(tasks)
            override fun getTasksByDate(date: String) = flowOf(tasks)
            override suspend fun getTaskById(id: Long) = tasks.find { it.id == id }
            override suspend fun insertTask(task: com.example.habitflow.data.entity.TaskEntity): Long { tasks.add(task); return tasks.size.toLong() }
            override suspend fun insertTasks(tasks: List<com.example.habitflow.data.entity.TaskEntity>) { this.tasks.addAll(tasks) }
            override suspend fun updateTask(task: com.example.habitflow.data.entity.TaskEntity) {
                val idx = tasks.indexOfFirst { it.id == task.id }
                if (idx != -1) tasks[idx] = task
            }
            override suspend fun deleteTask(task: com.example.habitflow.data.entity.TaskEntity) { tasks.removeIf { it.id == task.id } }
        }
        val taskRepo = com.example.habitflow.data.repository.TaskRepository(taskDao)

        val task = com.example.habitflow.data.entity.TaskEntity(id = 1L, title = "Task 1", estimatedMins = 20, isCompleted = false, date = "Today")
        taskDao.insertTask(task)

        // Complete task -> +40 XP (20 * 2), +1 streak (12 -> 13)
        taskRepo.toggleTaskCompletion(task, userRepo)

        val progress = fakeUserDao.getUserProgressSync()
        assertEquals(13, progress?.streakDays)
        assertEquals(1290, progress?.currentXp)
    }
}
