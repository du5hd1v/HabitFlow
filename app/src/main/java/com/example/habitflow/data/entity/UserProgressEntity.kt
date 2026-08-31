package com.example.habitflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey
    val id: Long = 1L,
    val level: Int = 5,
    val currentXp: Int = 1250,
    val maxXP: Int = 2000,
    val streakDays: Int = 12,
    val totalFocusHours: Double = 42.5
)
