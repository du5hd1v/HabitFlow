package com.example.habitflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val durationMins: Int,
    val timestamp: Long = System.currentTimeMillis()
)
