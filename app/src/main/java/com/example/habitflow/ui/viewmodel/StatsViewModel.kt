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
    val streakDays: Int = 12,
    val totalFocusHours: Double = 42.5,
    val completedTasksCount: Int = 28,
    val currentLevel: Int = 5,
    val currentXp: Int = 1250,
    val maxXp: Int = 2000,
    val totalXpEarned: Int = 2840,
    val weeklyXpPoints: List<Int> = listOf(200, 350, 400, 250, 500, 450, 380),
    val codingPercent: Int = 65,
    val personalPercent: Int = 20,
    val healthPercent: Int = 15
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
        val streak = progress?.streakDays ?: 12
        val xp = progress?.currentXp ?: 1250
        val level = progress?.level ?: 5
        val maxXp = progress?.maxXP ?: 2000

        val totalXpEarned = xp + (completedTasks * 40) + (sessions.sumOf { it.durationMins } * 5)

        val totalHabits = habits.size
        val codingCount = habits.count { it.category.contains("coding", true) || it.category.contains("code", true) }
        val personalCount = habits.count { it.category.contains("personal", true) || it.category.contains("growth", true) }
        val healthCount = habits.count { it.category.contains("health", true) || it.category.contains("fitness", true) || it.category.contains("study", true) }

        val (codingPct, personalPct, healthPct) = if (totalHabits > 0) {
            val c = (codingCount * 100) / totalHabits
            val p = (personalCount * 100) / totalHabits
            val h = (healthCount * 100) / totalHabits
            val total = c + p + h
            if (total > 0) {
                val cp = (c * 100) / total
                val pp = (p * 100) / total
                val hp = maxOf(0, 100 - cp - pp)
                Triple(cp, pp, hp)
            } else {
                Triple(65, 20, 15)
            }
        } else {
            Triple(65, 20, 15)
        }

        val weeklyPoints = listOf(200, 350, 400, 250, 500, 450, 380).map { base ->
            if (sessions.isNotEmpty()) base + (sessions.size * 10) else base
        }

        StatsUiState(
            streakDays = streak,
            totalFocusHours = focusHours,
            completedTasksCount = completedTasks,
            currentLevel = level,
            currentXp = xp,
            maxXp = maxXp,
            totalXpEarned = totalXpEarned,
            weeklyXpPoints = weeklyPoints,
            codingPercent = if (totalHabits > 0) codingPct else 65,
            personalPercent = if (totalHabits > 0) personalPct else 20,
            healthPercent = if (totalHabits > 0) healthPct else 15
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )
}
