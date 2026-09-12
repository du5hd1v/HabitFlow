package com.example.habitflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.data.entity.HabitEntity
import com.example.habitflow.data.entity.UserProgressEntity
import com.example.habitflow.data.repository.HabitRepository
import com.example.habitflow.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val habitRepository: HabitRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val habits: StateFlow<List<HabitEntity>> = habitRepository.allHabits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userProgress: StateFlow<UserProgressEntity> = userRepository.userProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProgressEntity(id = 1L, level = 5, currentXp = 1250, maxXP = 2000, streakDays = 12, totalFocusHours = 42.5)
        )

    fun toggleHabitCompletion(habit: HabitEntity) {
        viewModelScope.launch {
            val willBeCompleted = !habit.isCompleted
            habitRepository.toggleHabitCompletion(habit)
            if (willBeCompleted) {
                userRepository.addXpAndFocus(habit.xpReward, 0.0, streakDelta = 1)
            } else {
                userRepository.addXpAndFocus(-habit.xpReward, 0.0, streakDelta = -1)
            }
        }
    }

    fun calculateXpForCategory(category: String): Int {
        return when (category.trim().lowercase()) {
            "coding" -> 50
            "study" -> 45
            "health", "fitness" -> 40
            "work" -> 35
            "personal" -> 30
            else -> 25
        }
    }

    fun addHabit(title: String, category: String, details: String) {
        val normalizedCategory = category.ifEmpty { "General" }
        val xpReward = calculateXpForCategory(normalizedCategory)
        viewModelScope.launch {
            val newHabit = HabitEntity(
                title = title,
                category = normalizedCategory,
                xpReward = xpReward,
                dueDate = "Today",
                isCompleted = false,
                details = details
            )
            habitRepository.insertHabit(newHabit)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.updateHabit(habit)
        }
    }
}
