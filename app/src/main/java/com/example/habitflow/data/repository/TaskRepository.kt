package com.example.habitflow.data.repository

import com.example.habitflow.data.dao.TaskDao
import com.example.habitflow.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTasksByDate(date: String): Flow<List<TaskEntity>> = taskDao.getTasksByDate(date)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun toggleTaskCompletion(task: TaskEntity, userRepository: UserRepository? = null) {
        val willBeCompleted = !task.isCompleted
        val updated = task.copy(isCompleted = willBeCompleted)
        taskDao.updateTask(updated)
        if (userRepository != null) {
            val xp = if (task.estimatedMins > 0) task.estimatedMins * 2 else 25
            if (willBeCompleted) {
                userRepository.addXpAndFocus(xp, 0.0, streakDelta = 1)
            } else {
                userRepository.addXpAndFocus(-xp, 0.0, streakDelta = -1)
            }
        }
    }
}
