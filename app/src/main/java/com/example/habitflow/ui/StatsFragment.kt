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

        val tvCodingTitle = view.findViewById<TextView>(R.id.tv_category_coding_title)
        val progressCoding = view.findViewById<android.widget.ProgressBar>(R.id.progress_coding)
        val tvPersonalTitle = view.findViewById<TextView>(R.id.tv_category_personal_title)
        val progressPersonal = view.findViewById<android.widget.ProgressBar>(R.id.progress_personal)
        val tvHealthTitle = view.findViewById<TextView>(R.id.tv_category_health_title)
        val progressHealth = view.findViewById<android.widget.ProgressBar>(R.id.progress_health)

        val tvStreakTitle = view.findViewById<TextView>(R.id.tv_streak_calendar_title)
        val tvStreakBadge = view.findViewById<TextView>(R.id.tv_streak_badge)
        /*val dayViews = listOf(
            view.findViewById<TextView>(R.id.tv_day_1),
            view.findViewById<TextView>(R.id.tv_day_2),
            view.findViewById<TextView>(R.id.tv_day_3),
            view.findViewById<TextView>(R.id.tv_day_4),
            view.findViewById<TextView>(R.id.tv_day_5),
            view.findViewById<TextView>(R.id.tv_day_6),
            view.findViewById<TextView>(R.id.tv_day_7),
            view.findViewById<TextView>(R.id.tv_day_8),
            view.findViewById<TextView>(R.id.tv_day_9),
            view.findViewById<TextView>(R.id.tv_day_10),
            view.findViewById<TextView>(R.id.tv_day_11),
            view.findViewById<TextView>(R.id.tv_day_12)
        )*/

        val barViews = listOf(
            view.findViewById<View>(R.id.view_bar_mon),
            view.findViewById<View>(R.id.view_bar_tue),
            view.findViewById<View>(R.id.view_bar_wed),
            view.findViewById<View>(R.id.view_bar_thu),
            view.findViewById<View>(R.id.view_bar_fri),
            view.findViewById<View>(R.id.view_bar_sat),
            view.findViewById<View>(R.id.view_bar_sun)
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    tvTotalXp.text = "${state.totalXpEarned} XP Earned"
                    tvFocusHours.text = "${String.format(Locale.getDefault(), "%.1f", state.totalFocusHours)}h"
                    tvTasksCompleted.text = "${state.completedTasksCount}"

                    tvCodingTitle.text = "Coding & Engineering (${state.codingPercent}%)"
                    progressCoding.progress = state.codingPercent

                    tvPersonalTitle.text = "Personal Growth (${state.personalPercent}%)"
                    progressPersonal.progress = state.personalPercent

                    tvHealthTitle.text = "Health & Wellness (${state.healthPercent}%)"
                    progressHealth.progress = state.healthPercent

                    tvStreakBadge.text = "${state.streakDays} Day Streak 🔥"
                    tvStreakTitle.text = "STREAK CALENDAR"
                    /*dayViews.forEachIndexed { index, tv ->
                        val dayNum = index + 1
                        if (dayNum <= state.streakDays) {
                            tv.setBackgroundColor(android.graphics.Color.parseColor("#4F46E5"))
                            tv.setTextColor(android.graphics.Color.WHITE)
                        } else {
                            tv.setBackgroundColor(android.graphics.Color.parseColor("#E0E7FF"))
                            tv.setTextColor(android.graphics.Color.parseColor("#4F46E5"))
                        }
                    }*/

                    state.weeklyXpPoints.forEachIndexed { index, points ->
                        if (index < barViews.size) {
                            val bar = barViews[index]
                            val params = bar.layoutParams
                            val maxPoints = maxOf(600, state.weeklyXpPoints.maxOrNull() ?: 600)
                            val heightDp = ((points.toFloat() / maxPoints) * 100f).coerceIn(15f, 110f)
                            params.height = (heightDp * resources.displayMetrics.density).toInt()
                            bar.layoutParams = params
                        }
                    }
                }
            }
        }
    }
}
