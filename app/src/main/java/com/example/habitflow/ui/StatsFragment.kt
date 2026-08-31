package com.example.habitflow.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.habitflow.R
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.repository.HabitRepository
import com.example.habitflow.data.repository.StudyRepository
import com.example.habitflow.data.repository.TaskRepository
import com.example.habitflow.data.repository.UserRepository
import com.example.habitflow.ui.viewmodel.StatsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class StatsFragment : Fragment(R.layout.fragment_stats) {

    private lateinit var viewModel: StatsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return StatsViewModel(
                        StudyRepository(database.studySessionDao()),
                        TaskRepository(database.taskDao()),
                        UserRepository(database.userProgressDao()),
                        HabitRepository(database.habitDao())
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[StatsViewModel::class.java]

        val tvTotalXp = view.findViewById<TextView>(R.id.tv_total_xp_overview)
        val tvFocusHours = view.findViewById<TextView>(R.id.tv_stats_focus_hours)
        val tvTasksCompleted = view.findViewById<TextView>(R.id.tv_stats_tasks_completed)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    tvTotalXp.text = "${state.currentXp + 1590} XP Earned"
                    tvFocusHours.text = "${String.format(Locale.getDefault(), "%.1f", state.totalFocusHours)}h"
                    tvTasksCompleted.text = "${state.completedTasksCount}"
                }
            }
        }
    }
}
