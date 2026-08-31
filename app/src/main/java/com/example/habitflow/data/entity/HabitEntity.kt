package com.example.habitflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val category: String,
    val xpReward: Int,
    val dueDate: String,
    val isCompleted: Boolean = false,
    val details: String = ""
)
