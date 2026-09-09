package com.example.habitflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.data.entity.StudySessionEntity
import com.example.habitflow.data.repository.StudyRepository
import com.example.habitflow.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyUiState(
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val selectedDurationMins: Int = 25,
    val isRunning: Boolean = false,
    val selectedAudioPreset: String = "Lo-Fi Beats",
    val sessionTitle: String = "Deep Focus Study"
)

class StudyViewModel(
    private val studyRepository: StudyRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    fun setAudioPreset(preset: String) {
        _uiState.update { it.copy(selectedAudioPreset = preset) }
    }

    fun setSessionTitle(title: String) {
        _uiState.update { it.copy(sessionTitle = title) }
    }

    fun setDuration(minutes: Int) {
        pauseTimer()
        val secs = minutes * 60
        _uiState.update { 
            it.copy(
                totalSeconds = secs,
                remainingSeconds = secs,
                selectedDurationMins = minutes
            ) 
        }
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return
        _uiState.update { it.copy(isRunning = true) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning && _uiState.value.remainingSeconds > 0) {
                kotlinx.coroutines.delay(1000L)
                _uiState.update { state ->
                    val nextSec = state.remainingSeconds - 1
                    if (nextSec <= 0) {
                        onSessionCompleted()
                        state.copy(remainingSeconds = 0, isRunning = false)
                    } else {
                        state.copy(remainingSeconds = nextSec)
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        val mins = _uiState.value.selectedDurationMins
        val secs = mins * 60
        _uiState.update { it.copy(totalSeconds = secs, remainingSeconds = secs) }
    }

    fun resetTimer(customMinutes: Int) {
        setDuration(customMinutes)
    }

    private fun onSessionCompleted() {
        viewModelScope.launch {
            val durationMins = _uiState.value.totalSeconds / 60
            studyRepository.insertSession(
                StudySessionEntity(
                    title = _uiState.value.sessionTitle,
                    durationMins = durationMins,
                    timestamp = System.currentTimeMillis()
                )
            )
            // Reward +100 XP and focus hours
            userRepository.addXpAndFocus(100, durationMins / 60.0)
        }
    }
}
