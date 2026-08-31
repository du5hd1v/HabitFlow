package com.example.habitflow.data.repository

import com.example.habitflow.data.dao.StudySessionDao
import com.example.habitflow.data.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studySessionDao: StudySessionDao) {
    val allSessions: Flow<List<StudySessionEntity>> = studySessionDao.getAllSessions()

    suspend fun insertSession(session: StudySessionEntity): Long = studySessionDao.insertSession(session)

    suspend fun deleteSession(session: StudySessionEntity) = studySessionDao.deleteSession(session)
}
