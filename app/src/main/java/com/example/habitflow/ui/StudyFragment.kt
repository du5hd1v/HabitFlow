package com.example.habitflow.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.habitflow.R
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.repository.StudyRepository
import com.example.habitflow.data.repository.UserRepository
import com.example.habitflow.ui.viewmodel.StudyViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class StudyFragment : Fragment(R.layout.fragment_study) {

    private lateinit var viewModel: StudyViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return StudyViewModel(
                        StudyRepository(database.studySessionDao()),
                        UserRepository(database.userProgressDao())
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[StudyViewModel::class.java]

        val tvCountdown = view.findViewById<TextView>(R.id.tv_timer_countdown)
        val tvActiveTask = view.findViewById<TextView>(R.id.tv_active_task_title)
        val btnStart = view.findViewById<Button>(R.id.btn_timer_start)
        val btnPause = view.findViewById<Button>(R.id.btn_timer_pause)
        val btnReset = view.findViewById<Button>(R.id.btn_timer_reset)

        val btnRain = view.findViewById<Button>(R.id.btn_audio_rain)
        val btnLofi = view.findViewById<Button>(R.id.btn_audio_lofi)
        val btnCafe = view.findViewById<Button>(R.id.btn_audio_cafe)

        btnStart.setOnClickListener { viewModel.startTimer() }
        btnPause.setOnClickListener { viewModel.pauseTimer() }
        btnReset.setOnClickListener { viewModel.resetTimer(25) }

        btnRain.setOnClickListener { viewModel.setAudioPreset("Rain") }
        btnLofi.setOnClickListener { viewModel.setAudioPreset("Lo-Fi Beats") }
        btnCafe.setOnClickListener { viewModel.setAudioPreset("Cafe") }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    val mins = state.remainingSeconds / 60
                    val secs = state.remainingSeconds % 60
                    tvCountdown.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                    tvActiveTask.text = "Deep Focus Session - ${state.sessionTitle}"
                }
            }
        }
    }
}
