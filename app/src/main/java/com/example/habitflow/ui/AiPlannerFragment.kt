package com.example.habitflow.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.R
import com.example.habitflow.data.AppDatabase
import com.example.habitflow.data.repository.GeminiRepository
import com.example.habitflow.data.repository.HabitRepository
import com.example.habitflow.ui.adapter.AiStepsAdapter
import com.example.habitflow.ui.viewmodel.AiPlannerViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AiPlannerFragment : Fragment(R.layout.fragment_ai_planner) {

    private lateinit var viewModel: AiPlannerViewModel
    private lateinit var stepsAdapter: AiStepsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AiPlannerViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AiPlannerViewModel(
                        GeminiRepository(),
                        HabitRepository(database.habitDao())
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[AiPlannerViewModel::class.java]

        val etPrompt = view.findViewById<TextInputEditText>(R.id.et_ai_prompt)
        val btnGenerate = view.findViewById<Button>(R.id.btn_generate_steps)
        val btnAddAll = view.findViewById<Button>(R.id.btn_add_all_steps)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_ai)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_ai_steps)

        stepsAdapter = AiStepsAdapter(
            onAddStep = { taskItem -> 
                viewModel.saveGeneratedTaskToToday(taskItem)
                android.widget.Toast.makeText(requireContext(), "Added '${taskItem.title}' to Today's Tasks", android.widget.Toast.LENGTH_SHORT).show()
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = stepsAdapter

        etPrompt.doAfterTextChanged { text ->
            viewModel.updateGoalInput(text?.toString() ?: "")
        }

        btnGenerate.setOnClickListener {
            viewModel.generatePlan()
            android.widget.Toast.makeText(requireContext(), "Generating AI study steps...", android.widget.Toast.LENGTH_SHORT).show()
        }

        btnAddAll.setOnClickListener {
            viewModel.saveAllGeneratedTasks()
            android.widget.Toast.makeText(requireContext(), "Added all AI steps to Today's Tasks!", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Dynamic Hint Cycler for TextInputEditText
        val hintExamples = listOf(
            "How do I study for my upcoming assignment?",
            "Create a 4-week Kotlin mastery plan",
            "Break down my final year project roadmap",
            "How do I build consistent daily reading habits?",
            "How do I study for my upcoming mid exam?"
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var hintIndex = 0
                while (true) {
                    if (etPrompt.text.isNullOrEmpty() && !etPrompt.hasFocus()) {
                        etPrompt.animate()
                            .alpha(0.3f)
                            .setDuration(250)
                            .withEndAction {
                                etPrompt.hint = hintExamples[hintIndex]
                                etPrompt.animate()
                                    .alpha(1.0f)
                                    .setDuration(250)
                                    .start()
                            }
                            .start()
                    }
                    kotlinx.coroutines.delay(4000L)
                    hintIndex = (hintIndex + 1) % hintExamples.size
                }
            }
        }

        var lastShownError: String? = null
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    if (etPrompt.text.toString() != state.goalInput && state.goalInput.isNotEmpty()) {
                        etPrompt.setText(state.goalInput)
                    }
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    stepsAdapter.submitList(state.generatedTasks)

                    if (!state.errorMessage.isNullOrEmpty() && state.errorMessage != lastShownError) {
                        lastShownError = state.errorMessage
                        android.widget.Toast.makeText(requireContext(), state.errorMessage, android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
