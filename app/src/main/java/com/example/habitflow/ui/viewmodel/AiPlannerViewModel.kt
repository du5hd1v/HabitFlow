package com.example.habitflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.data.entity.HabitEntity
import com.example.habitflow.data.remote.GeneratedTaskItem
import com.example.habitflow.data.repository.GeminiRepository
import com.example.habitflow.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiPlannerUiState(
    val goalInput: String = "",
    val isLoading: Boolean = false,
    val generatedTasks: List<GeneratedTaskItem> = emptyList(),
    val errorMessage: String? = null
)

class AiPlannerViewModel(
    private val geminiRepository: GeminiRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiPlannerUiState())
    val uiState: StateFlow<AiPlannerUiState> = _uiState.asStateFlow()

    fun updateGoalInput(goal: String) {
        _uiState.update { it.copy(goalInput = goal) }
    }

    fun generatePlan() {
        val goal = _uiState.value.goalInput.trim()
        if (goal.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter a study goal or topic") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val tasks = geminiRepository.generateStudyPlan(goal)
                _uiState.update { it.copy(isLoading = false, generatedTasks = tasks) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Failed to generate plan") }
            }
        }
    }

    fun saveGeneratedTaskToToday(taskItem: GeneratedTaskItem) {
        viewModelScope.launch {
            val xp = if (taskItem.estimatedMins > 0) taskItem.estimatedMins * 2 else 45
            val entity = HabitEntity(
                title = taskItem.title,
                category = "Study",
                xpReward = xp,
                dueDate = "Today",
                isCompleted = false,
                details = "Est. ${taskItem.estimatedMins} mins"
            )
            habitRepository.insertHabit(entity)
        }
    }

    fun saveAllGeneratedTasks() {
        viewModelScope.launch {
            for (item in _uiState.value.generatedTasks) {
                val xp = if (item.estimatedMins > 0) item.estimatedMins * 2 else 45
                val entity = HabitEntity(
                    title = item.title,
                    category = "Study",
                    xpReward = xp,
                    dueDate = "Today",
                    isCompleted = false,
                    details = "Est. ${item.estimatedMins} mins"
                )
                habitRepository.insertHabit(entity)
            }
        }
    }
}
