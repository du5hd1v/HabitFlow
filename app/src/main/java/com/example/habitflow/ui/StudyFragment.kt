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
        val factory = object : ViewModelProvider.AndroidViewModelFactory(requireActivity().application) {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return StudyViewModel(
                        requireActivity().application,
                        StudyRepository(database.studySessionDao()),
                        UserRepository(database.userProgressDao())
                    ) as T
                }
                return super.create(modelClass)
            }
        }
        viewModel = ViewModelProvider(this, factory)[StudyViewModel::class.java]

        val tvCountdown = view.findViewById<TextView>(R.id.tv_timer_countdown)
        val tvTimerLabel = view.findViewById<TextView>(R.id.tv_timer_label)
        val tvActiveTask = view.findViewById<TextView>(R.id.tv_active_task_title)
        val btnStart = view.findViewById<Button>(R.id.btn_timer_start)
        val btnPause = view.findViewById<Button>(R.id.btn_timer_pause)
        val btnReset = view.findViewById<Button>(R.id.btn_timer_reset)

        val btnRain = view.findViewById<Button>(R.id.btn_audio_rain)
        val btnLofi = view.findViewById<Button>(R.id.btn_audio_lofi)
        val btnCafe = view.findViewById<Button>(R.id.btn_audio_cafe)

        val btnDur15 = view.findViewById<Button>(R.id.btn_duration_15)
        val btnDur25 = view.findViewById<Button>(R.id.btn_duration_25)
        val btnDur30 = view.findViewById<Button>(R.id.btn_duration_30)
        val btnDur45 = view.findViewById<Button>(R.id.btn_duration_45)
        val btnDur60 = view.findViewById<Button>(R.id.btn_duration_60)
        val btnDurCustom = view.findViewById<Button>(R.id.btn_duration_custom)

        btnStart.setOnClickListener { 
            viewModel.startTimer()
            android.widget.Toast.makeText(requireContext(), "Pomodoro Timer Started", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnPause.setOnClickListener { 
            viewModel.pauseTimer()
            android.widget.Toast.makeText(requireContext(), "Pomodoro Timer Paused", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnReset.setOnClickListener { 
            viewModel.resetTimer()
            android.widget.Toast.makeText(requireContext(), "Pomodoro Timer Reset", android.widget.Toast.LENGTH_SHORT).show()
        }

        btnDur15.setOnClickListener {
            viewModel.setDuration(15)
            android.widget.Toast.makeText(requireContext(), "Duration set to 15 minutes", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnDur25.setOnClickListener {
            viewModel.setDuration(25)
            android.widget.Toast.makeText(requireContext(), "Duration set to 25 minutes", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnDur30.setOnClickListener {
            viewModel.setDuration(30)
            android.widget.Toast.makeText(requireContext(), "Duration set to 30 minutes", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnDur45.setOnClickListener {
            viewModel.setDuration(45)
            android.widget.Toast.makeText(requireContext(), "Duration set to 45 minutes", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnDur60.setOnClickListener {
            viewModel.setDuration(60)
            android.widget.Toast.makeText(requireContext(), "Duration set to 60 minutes", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnDurCustom.setOnClickListener {
            val customDur = viewModel.uiState.value.customDurationMins
            if (customDur == null || viewModel.uiState.value.selectedDurationMins == customDur) {
                showCustomDurationDialog()
            } else {
                viewModel.setCustomDuration(customDur)
                android.widget.Toast.makeText(requireContext(), "Duration set to $customDur minutes", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        btnDurCustom.setOnLongClickListener {
            showCustomDurationDialog()
            true
        }

        btnRain.setOnClickListener { 
            viewModel.setAudioPreset("Rain")
            android.widget.Toast.makeText(requireContext(), "Ambient Audio: Rain", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnLofi.setOnClickListener { 
            viewModel.setAudioPreset("Lo-Fi Beats")
            android.widget.Toast.makeText(requireContext(), "Ambient Audio: Lo-Fi Beats", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnCafe.setOnClickListener { 
            viewModel.setAudioPreset("Cafe")
            android.widget.Toast.makeText(requireContext(), "Ambient Audio: Cafe", android.widget.Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    val remaining = state.remainingSeconds
                    val hours = remaining / 3600
                    val mins = (remaining % 3600) / 60
                    val secs = remaining % 60

                    if (hours > 0) {
                        tvCountdown.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs)
                        tvTimerLabel.text = "HOURS / MINUTES LEFT"
                    } else {
                        tvCountdown.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                        tvTimerLabel.text = "MINUTES LEFT"
                    }

                    tvActiveTask.text = "Deep Focus Session - ${state.sessionTitle}"

                    // Highlight duration presets
                    val duration = state.selectedDurationMins
                    val customDur = state.customDurationMins
                    updateButtonSelection(btnDur15, duration == 15)
                    updateButtonSelection(btnDur25, duration == 25)
                    updateButtonSelection(btnDur30, duration == 30)
                    updateButtonSelection(btnDur45, duration == 45)
                    updateButtonSelection(btnDur60, duration == 60)

                    val isCustomActive = customDur != null && duration == customDur
                    updateButtonSelection(btnDurCustom, isCustomActive)

                    if (customDur != null) {
                        btnDurCustom.text = "Custom (${customDur}m)"
                    } else {
                        btnDurCustom.text = "Custom"
                    }

                    // Highlight ambient audio presets
                    val audio = state.selectedAudioPreset
                    updateButtonSelection(btnRain, audio == "Rain")
                    updateButtonSelection(btnLofi, audio == "Lo-Fi Beats")
                    updateButtonSelection(btnCafe, audio == "Cafe")
                }
            }
        }
    }

    private fun updateButtonSelection(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#8B5CF6")) // Vibrant Electric Violet
            button.setTextColor(android.graphics.Color.WHITE)
            (button as? com.google.android.material.button.MaterialButton)?.let {
                it.strokeWidth = 0
            }
        } else {
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.habit_surface_light))
            button.setTextColor(android.graphics.Color.BLACK)
            (button as? com.google.android.material.button.MaterialButton)?.let {
                it.strokeWidth = 2
                it.strokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#94A3B8"))
            }
        }
    }

    private fun showCustomDurationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Custom Focus Duration")

        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Enter minutes (e.g. 75)"

        val container = android.widget.FrameLayout(requireContext())
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = 48
        params.rightMargin = 48
        input.layoutParams = params
        container.addView(input)

        builder.setView(container)

        builder.setPositiveButton("Set") { _, _ ->
            val text = input.text.toString()
            val mins = text.toIntOrNull()
            if (mins != null && mins > 0) {
                viewModel.setCustomDuration(mins)
                android.widget.Toast.makeText(requireContext(), "Duration set to $mins minutes", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(requireContext(), "Invalid duration entered", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }
}
