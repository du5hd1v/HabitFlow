package com.example.habitflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.data.PreferencesManager
import com.example.habitflow.data.entity.TaskEntity
import com.example.habitflow.data.remote.GeneratedTaskItem
import com.example.habitflow.data.repository.GeminiRepository
import com.example.habitflow.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiPlannerUiState(
    val goalInput: String = "",
    val isLoading: Boolean = false,
    val generatedTasks: List<GeneratedTaskItem> = emptyList(),
    val apiKey: String = "",
    val errorMessage: String? = null
)

class AiPlannerViewModel(
    private val geminiRepository: GeminiRepository,
    private val taskRepository: TaskRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiPlannerUiState())
    val uiState: StateFlow<AiPlannerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val key = preferencesManager.geminiApiKeyFlow.first()
            _uiState.update { it.copy(apiKey = key) }
        }
    }

    fun updateGoalInput(goal: String) {
        _uiState.update { it.copy(goalInput = goal) }
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key) }
        viewModelScope.launch {
            preferencesManager.saveApiKey(key)
        }
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
                val tasks = geminiRepository.generateStudyPlan(goal, _uiState.value.apiKey)
                _uiState.update { it.copy(isLoading = false, generatedTasks = tasks) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Failed to generate plan") }
            }
        }
    }

    fun saveGeneratedTaskToToday(taskItem: GeneratedTaskItem) {
        viewModelScope.launch {
            val entity = TaskEntity(
                title = taskItem.title,
                estimatedMins = taskItem.estimatedMins,
                isCompleted = false,
                date = "Today"
            )
            taskRepository.insertTask(entity)
        }
    }

    fun saveAllGeneratedTasks() {
        viewModelScope.launch {
            for (item in _uiState.value.generatedTasks) {
                val entity = TaskEntity(
                    title = item.title,
                    estimatedMins = item.estimatedMins,
                    isCompleted = false,
                    date = "Today"
                )
                taskRepository.insertTask(entity)
            }
        }
    }
}
