package com.example.habitflow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import com.example.habitflow.R
import com.example.habitflow.data.entity.HabitEntity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class AddHabitBottomSheetFragment(
    private val habitToEdit: HabitEntity? = null,
    private val onSave: (String, String, String) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_habit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvDialogTitle = view.findViewById<TextView>(R.id.tv_dialog_title)
        val etTitle = view.findViewById<TextInputEditText>(R.id.et_title)
        val etCategory = view.findViewById<AutoCompleteTextView>(R.id.et_category)
        val etDetails = view.findViewById<TextInputEditText>(R.id.et_details)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        val categories = listOf("Coding", "Study", "Health", "Personal", "Work", "Fitness")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)

        if (habitToEdit != null) {
            tvDialogTitle?.text = "Edit Habit / Task"
            etTitle.setText(habitToEdit.title)
            etCategory.setText(habitToEdit.category, false)
            etDetails.setText(habitToEdit.details)
            btnSave.text = "Update Habit / Task"
        } else {
            if (categories.isNotEmpty()) {
                etCategory.setText(categories[0], false)
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val category = etCategory.text.toString().trim().ifEmpty { "General" }
            val details = etDetails.text?.toString()?.trim().let { if (it.isNullOrEmpty()) "Custom Task" else it }
            if (title.isNotEmpty()) {
                onSave(title, category, details)
                dismiss()
            }
        }
    }
}
