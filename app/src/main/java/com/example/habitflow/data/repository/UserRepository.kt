package com.example.habitflow.data.repository

import com.example.habitflow.data.dao.UserProgressDao
import com.example.habitflow.data.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userProgressDao: UserProgressDao) {
    val userProgress: Flow<UserProgressEntity?> = userProgressDao.getUserProgress()

    suspend fun getUserProgressSync(): UserProgressEntity? = userProgressDao.getUserProgressSync()

    suspend fun updateProgress(progress: UserProgressEntity) = userProgressDao.updateUserProgress(progress)

    suspend fun addXpAndFocus(xpGained: Int, focusHoursGained: Double) {
        val current = userProgressDao.getUserProgressSync() ?: UserProgressEntity()
        var newXp = current.currentXp + xpGained
        var newLevel = current.level
        var newMaxXp = current.maxXP

        if (newXp >= newMaxXp) {
            newLevel += 1
            newXp -= newMaxXp
            newMaxXp = (newMaxXp * 1.2).toInt()
        }

        val updated = current.copy(
            level = newLevel,
            currentXp = newXp,
            maxXP = newMaxXp,
            totalFocusHours = current.totalFocusHours + focusHoursGained
        )
        userProgressDao.updateUserProgress(updated)
    }
}
