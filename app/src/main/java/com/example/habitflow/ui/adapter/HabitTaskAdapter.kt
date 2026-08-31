package com.example.habitflow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.R
import com.example.habitflow.data.entity.HabitEntity

class HabitTaskAdapter(
    private val onToggle: (HabitEntity) -> Unit,
    private val onDelete: (HabitEntity) -> Unit
) : ListAdapter<HabitEntity, HabitTaskAdapter.HabitViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit_task, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_complete)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category_pill)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvSubInfo: TextView = itemView.findViewById(R.id.tv_sub_info)
        private val btnOptions: ImageButton = itemView.findViewById(R.id.btn_options)

        fun bind(habit: HabitEntity) {
            tvTitle.text = habit.title
            tvCategory.text = "${habit.category} +${habit.xpReward} XP"
            tvSubInfo.text = "Due ${habit.dueDate} • ${habit.details.ifEmpty { "Active Habit" }}"
            checkbox.isChecked = habit.isCompleted

            checkbox.setOnClickListener {
                onToggle(habit)
            }

            btnOptions.setOnClickListener {
                onDelete(habit)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HabitEntity>() {
        override fun areItemsTheSame(oldItem: HabitEntity, newItem: HabitEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HabitEntity, newItem: HabitEntity): Boolean = oldItem == newItem
    }
}
