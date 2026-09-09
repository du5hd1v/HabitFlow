package com.example.habitflow.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.R
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.repository.HabitRepository
import com.example.habitflow.data.repository.UserRepository
import com.example.habitflow.ui.adapter.HabitTaskAdapter
import com.example.habitflow.ui.dialog.AddHabitBottomSheetFragment
import com.example.habitflow.ui.viewmodel.HomeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: HomeViewModel
    private lateinit var habitAdapter: HabitTaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return HomeViewModel(
                        HabitRepository(database.habitDao()),
                        UserRepository(database.userProgressDao())
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        val tvUserName = view.findViewById<TextView>(R.id.tv_user_name)
        val tvUserLevel = view.findViewById<TextView>(R.id.tv_user_level)
        val tvXpProgress = view.findViewById<TextView>(R.id.tv_xp_progress)
        val progressXp = view.findViewById<ProgressBar>(R.id.progress_xp)
        val tvStreak = view.findViewById<TextView>(R.id.tv_streak_metric)
        val tvFocus = view.findViewById<TextView>(R.id.tv_focus_metric)
        val btnStartRecommended = view.findViewById<Button>(R.id.btn_start_recommended)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add_habit)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_habits)
        val containerUserHeader = view.findViewById<View>(R.id.container_user_header)

        val headerPadding = (20 * resources.displayMetrics.density).toInt()
        containerUserHeader.setPadding(headerPadding, headerPadding, headerPadding, headerPadding)

        tvUserName.text = "Alex Rivera"

        habitAdapter = HabitTaskAdapter(
            onToggle = { habit -> viewModel.toggleHabitCompletion(habit) },
            onEdit = { habit ->
                val bottomSheet = AddHabitBottomSheetFragment(habit) { title, category, details ->
                    val updated = habit.copy(
                        title = title,
                        category = category,
                        details = details,
                        xpReward = viewModel.calculateXpForCategory(category)
                    )
                    viewModel.updateHabit(updated)
                }
                bottomSheet.show(parentFragmentManager, "AddHabitBottomSheet")
            },
            onDelete = { habit -> viewModel.deleteHabit(habit) }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = habitAdapter

        btnStartRecommended.setOnClickListener {
            findNavController().navigate(R.id.navigation_study)
        }

        fabAdd.setOnClickListener {
            val bottomSheet = AddHabitBottomSheetFragment { title, category, details ->
                viewModel.addHabit(title, category, details)
            }
            bottomSheet.show(parentFragmentManager, "AddHabitBottomSheet")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.habits.collectLatest { list ->
                        habitAdapter.submitList(list)
                    }
                }
                launch {
                    viewModel.userProgress.collectLatest { progress ->
                        progress?.let {
                            tvUserLevel.text = "Level ${it.level} Scholar"
                            tvXpProgress.text = "${it.currentXp} / ${it.maxXP} XP"
                            val percent = if (it.maxXP > 0) (it.currentXp * 100 / it.maxXP) else 0
                            progressXp.progress = percent
                            tvStreak.text = "${it.streakDays} Days"
                            tvFocus.text = "${String.format(Locale.getDefault(), "%.1f", it.totalFocusHours)}h"
                        }
                    }
                }
            }
        }
    }
}
