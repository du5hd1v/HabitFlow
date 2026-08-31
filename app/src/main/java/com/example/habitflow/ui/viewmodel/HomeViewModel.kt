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

    val userProgress: StateFlow<UserProgressEntity?> = userRepository.userProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleHabitCompletion(habit: HabitEntity) {
        viewModelScope.launch {
            val willBeCompleted = !habit.isCompleted
            habitRepository.toggleHabitCompletion(habit)
            if (willBeCompleted) {
                userRepository.addXpAndFocus(habit.xpReward, 0.25)
            } else {
                // Optionally subtract or leave XP as earned bonus
            }
        }
    }

    fun addHabit(title: String, category: String, xpReward: Int, details: String) {
        viewModelScope.launch {
            val newHabit = HabitEntity(
                title = title,
                category = category,
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
}
