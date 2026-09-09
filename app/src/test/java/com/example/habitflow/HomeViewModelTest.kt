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
        override fun getAllHabits(): Flow<List<HabitEntity>> = flowOf(emptyList())
        override suspend fun getHabitById(id: Long): HabitEntity? = null
        override suspend fun insertHabit(habit: HabitEntity): Long = 1L
        override suspend fun insertHabits(habits: List<HabitEntity>) {}
        override suspend fun updateHabit(habit: HabitEntity) {}
        override suspend fun deleteHabit(habit: HabitEntity) {}
        override suspend fun deleteAllHabits() {}
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
}
