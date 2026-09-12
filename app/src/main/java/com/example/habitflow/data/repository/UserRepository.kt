package com.example.habitflow.data.repository

import com.example.habitflow.data.dao.UserProgressDao
import com.example.habitflow.data.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userProgressDao: UserProgressDao) {
    val userProgress: Flow<UserProgressEntity> = userProgressDao.getUserProgress()
        .map { it ?: UserProgressEntity(id = 1L, level = 5, currentXp = 1250, maxXP = 2000, streakDays = 12, totalFocusHours = 42.5) }

    suspend fun getUserProgressSync(): UserProgressEntity? = userProgressDao.getUserProgressSync()

    suspend fun updateProgress(progress: UserProgressEntity) = userProgressDao.insertUserProgress(progress)

    suspend fun addXpAndFocus(xpGained: Int, focusHoursGained: Double, streakDelta: Int = 0) {
        val current = userProgressDao.getUserProgressSync() ?: UserProgressEntity(
            id = 1L,
            level = 5,
            currentXp = 1250,
            maxXP = 2000,
            streakDays = 12,
            totalFocusHours = 42.5
        )
        var newXp = current.currentXp + xpGained
        var newLevel = current.level
        var newMaxXp = current.maxXP

        while (newXp >= newMaxXp) {
            newLevel += 1
            newXp -= newMaxXp
            newMaxXp = (newMaxXp * 1.2).toInt()
        }

        while (newXp < 0 && newLevel > 1) {
            newLevel -= 1
            newMaxXp = (newMaxXp / 1.2).toInt()
            if (newMaxXp < 1) newMaxXp = 100
            newXp = newMaxXp + newXp
        }

        if (newLevel == 1 && newXp < 0) {
            newXp = 0
        }

        val newStreak = maxOf(0, current.streakDays + streakDelta)

        val updated = current.copy(
            level = newLevel,
            currentXp = newXp,
            maxXP = newMaxXp,
            streakDays = newStreak,
            totalFocusHours = maxOf(0.0, current.totalFocusHours + focusHoursGained)
        )
        userProgressDao.insertUserProgress(updated)
    }
}
