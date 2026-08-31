package com.example.habitflow.data.repository

import com.example.habitflow.data.dao.HabitDao
import com.example.habitflow.data.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    suspend fun getHabitById(id: Long): HabitEntity? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)

    suspend fun toggleHabitCompletion(habit: HabitEntity) {
        val updated = habit.copy(isCompleted = !habit.isCompleted)
        habitDao.updateHabit(updated)
    }
}
