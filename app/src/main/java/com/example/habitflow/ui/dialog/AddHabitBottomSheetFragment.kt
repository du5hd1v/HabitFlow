package com.example.habitflow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.habitflow.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class AddHabitBottomSheetFragment(
    private val onSave: (String, String, Int, String) -> Unit
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

        val etTitle = view.findViewById<TextInputEditText>(R.id.et_title)
        val etCategory = view.findViewById<TextInputEditText>(R.id.et_category)
        val etXp = view.findViewById<TextInputEditText>(R.id.et_xp)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val category = etCategory.text.toString().trim().ifEmpty { "General" }
            val xp = etXp.text.toString().toIntOrNull() ?: 50
            if (title.isNotEmpty()) {
                onSave(title, category, xp, "Custom Task")
                dismiss()
            }
        }
    }
}
