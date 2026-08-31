package com.example.habitflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.data.repository.HabitRepository
import com.example.habitflow.data.repository.StudyRepository
import com.example.habitflow.data.repository.TaskRepository
import com.example.habitflow.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val streakDays: Int = 0,
    val totalFocusHours: Double = 0.0,
    val completedTasksCount: Int = 0,
    val currentLevel: Int = 1,
    val currentXp: Int = 0,
    val maxXp: Int = 1000,
    val weeklyXpPoints: List<Int> = listOf(150, 300, 200, 450, 400, 250, 350)
)

class StatsViewModel(
    studyRepository: StudyRepository,
    taskRepository: TaskRepository,
    userRepository: UserRepository,
    habitRepository: HabitRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        userRepository.userProgress,
        taskRepository.allTasks,
        studyRepository.allSessions,
        habitRepository.allHabits
    ) { progress, tasks, sessions, habits ->
        val completedTasks = tasks.count { it.isCompleted } + habits.count { it.isCompleted }
        val focusHours = progress?.totalFocusHours ?: ((sessions.sumOf { it.durationMins }) / 60.0)

        StatsUiState(
            streakDays = progress?.streakDays ?: 12,
            totalFocusHours = focusHours,
            completedTasksCount = completedTasks,
            currentLevel = progress?.level ?: 5,
            currentXp = progress?.currentXp ?: 1250,
            maxXp = progress?.maxXP ?: 2000,
            weeklyXpPoints = listOf(200, 350, 400, 250, 500, 450, 380)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )
}
