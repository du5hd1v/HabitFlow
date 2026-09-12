package com.example.habitflow

import android.app.Application
import com.example.habitflow.data.dao.StudySessionDao
import com.example.habitflow.data.dao.UserProgressDao
import com.example.habitflow.data.entity.StudySessionEntity
import com.example.habitflow.data.entity.UserProgressEntity
import com.example.habitflow.data.repository.StudyRepository
import com.example.habitflow.data.repository.UserRepository
import com.example.habitflow.ui.viewmodel.StudyViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

class StudyViewModelTest {

    private class FakeApplication : Application()

    private class FakeStudySessionDao : StudySessionDao {
        var sessions = mutableListOf<StudySessionEntity>()
        override fun getAllSessions(): Flow<List<StudySessionEntity>> = flowOf(sessions)
        override suspend fun insertSession(session: StudySessionEntity): Long {
            sessions.add(session)
            return sessions.size.toLong()
        }
        override suspend fun deleteSession(session: StudySessionEntity) {
            sessions.remove(session)
        }
    }

    private class FakeUserProgressDao : UserProgressDao {
        var currentProgress = UserProgressEntity(id = 1L, level = 1, currentXp = 0, maxXP = 100, streakDays = 0, totalFocusHours = 0.0)
        override fun getUserProgress(): Flow<UserProgressEntity?> = flowOf(currentProgress)
        override suspend fun getUserProgressSync(): UserProgressEntity = currentProgress
        override suspend fun insertUserProgress(progress: UserProgressEntity) { currentProgress = progress }
        override suspend fun updateUserProgress(progress: UserProgressEntity) { currentProgress = progress }
    }

    @Test
    fun testSetCustomDurationUpdatesState() {
        val app = FakeApplication()
        val studyRepo = StudyRepository(FakeStudySessionDao())
        val userRepo = UserRepository(FakeUserProgressDao())

        val viewModel = StudyViewModel(app, studyRepo, userRepo)
        assertEquals(25, viewModel.uiState.value.selectedDurationMins)
        assertEquals(null, viewModel.uiState.value.customDurationMins)

        viewModel.setCustomDuration(75)

        assertEquals(75, viewModel.uiState.value.selectedDurationMins)
        assertEquals(75, viewModel.uiState.value.customDurationMins)
        assertEquals(75 * 60, viewModel.uiState.value.totalSeconds)
        assertEquals(75 * 60, viewModel.uiState.value.remainingSeconds)
    }

    @Test
    fun testHoursAndMinutesFormattingLogic() {
        // 90 minutes = 5400 seconds (>= 3600 seconds) -> 01:30:00
        val remaining1 = 5400
        val hours1 = remaining1 / 3600
        val mins1 = (remaining1 % 3600) / 60
        val secs1 = remaining1 % 60
        val formatted1 = String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours1, mins1, secs1)
        assertEquals("01:30:00", formatted1)

        // 25 minutes = 1500 seconds (< 3600 seconds) -> 25:00
        val remaining2 = 1500
        val mins2 = remaining2 / 60
        val secs2 = remaining2 % 60
        val formatted2 = String.format(java.util.Locale.getDefault(), "%02d:%02d", mins2, secs2)
        assertEquals("25:00", formatted2)
    }
}
