package com.example.habitflow

import com.example.habitflow.data.dao.HabitDao
import com.example.habitflow.data.entity.HabitEntity
import com.example.habitflow.data.remote.GeneratedTaskItem
import com.example.habitflow.data.repository.GeminiRepository
import com.example.habitflow.data.repository.HabitRepository
import com.example.habitflow.ui.viewmodel.AiPlannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiPlannerViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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

    private class FakeGeminiRepository : GeminiRepository() {
        var customTasks = listOf(
            GeneratedTaskItem("Test Task 1", 15),
            GeneratedTaskItem("Test Task 2", 30)
        )

        override suspend fun generateStudyPlan(goal: String): List<GeneratedTaskItem> {
            return customTasks
        }
    }

    @Test
    fun testUpdateGoalInput() {
        val geminiRepo = FakeGeminiRepository()
        val habitRepo = HabitRepository(FakeHabitDao())
        val viewModel = AiPlannerViewModel(geminiRepo, habitRepo)

        assertEquals("", viewModel.uiState.value.goalInput)

        viewModel.updateGoalInput("Learn Android Architecture Components")
        assertEquals("Learn Android Architecture Components", viewModel.uiState.value.goalInput)
    }

    @Test
    fun testGeneratePlanSuccess() = runTest(testDispatcher) {
        val geminiRepo = FakeGeminiRepository()
        val habitRepo = HabitRepository(FakeHabitDao())
        val viewModel = AiPlannerViewModel(geminiRepo, habitRepo)

        viewModel.updateGoalInput("Master Jetpack Compose")
        viewModel.generatePlan()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(null, state.errorMessage)
        assertEquals(2, state.generatedTasks.size)
        assertEquals("Test Task 1", state.generatedTasks[0].title)
        assertEquals(15, state.generatedTasks[0].estimatedMins)
    }

    @Test
    fun testGeneratePlanEmptyGoalError() = runTest(testDispatcher) {
        val geminiRepo = FakeGeminiRepository()
        val habitRepo = HabitRepository(FakeHabitDao())
        val viewModel = AiPlannerViewModel(geminiRepo, habitRepo)

        viewModel.updateGoalInput("")
        viewModel.generatePlan()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("Please enter a study goal or topic", state.errorMessage)
        assertTrue(state.generatedTasks.isEmpty())
    }

    @Test
    fun testSaveGeneratedTaskToToday() = runTest(testDispatcher) {
        val fakeHabitDao = FakeHabitDao()
        val habitRepo = HabitRepository(fakeHabitDao)
        val geminiRepo = FakeGeminiRepository()
        val viewModel = AiPlannerViewModel(geminiRepo, habitRepo)

        val taskItem = GeneratedTaskItem("Practice Kotlin Coroutines", 25)
        viewModel.saveGeneratedTaskToToday(taskItem)
        advanceUntilIdle()

        assertEquals(1, fakeHabitDao.habits.size)
        assertEquals("Practice Kotlin Coroutines", fakeHabitDao.habits[0].title)
        assertEquals("Study", fakeHabitDao.habits[0].category)
        assertEquals(50, fakeHabitDao.habits[0].xpReward) // 25 * 2
        assertEquals("Today", fakeHabitDao.habits[0].dueDate)
        assertEquals("Est. 25 mins", fakeHabitDao.habits[0].details)
    }

    @Test
    fun testSaveAllGeneratedTasks() = runTest(testDispatcher) {
        val fakeHabitDao = FakeHabitDao()
        val habitRepo = HabitRepository(fakeHabitDao)
        val geminiRepo = FakeGeminiRepository()
        val viewModel = AiPlannerViewModel(geminiRepo, habitRepo)

        viewModel.updateGoalInput("Master Jetpack Compose")
        viewModel.generatePlan()
        advanceUntilIdle()

        viewModel.saveAllGeneratedTasks()
        advanceUntilIdle()

        assertEquals(2, fakeHabitDao.habits.size)
        assertEquals("Test Task 1", fakeHabitDao.habits[0].title)
        assertEquals("Test Task 2", fakeHabitDao.habits[1].title)
    }
}
