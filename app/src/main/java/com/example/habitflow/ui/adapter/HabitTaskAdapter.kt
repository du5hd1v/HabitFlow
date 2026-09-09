package com.example.habitflow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.R
import com.example.habitflow.data.entity.HabitEntity

class HabitTaskAdapter(
    private val onToggle: (HabitEntity) -> Unit,
    private val onEdit: (HabitEntity) -> Unit,
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

            val iconRes = when {
                habit.category.contains("Coding", true) || habit.category.contains("Code", true) -> R.drawable.ic_category_coding
                habit.category.contains("Health", true) || habit.category.contains("Fitness", true) || habit.category.contains("Wellness", true) -> R.drawable.ic_category_health
                habit.category.contains("Study", true) -> R.drawable.ic_study
                else -> R.drawable.ic_category_personal
            }
            val drawable = ContextCompat.getDrawable(itemView.context, iconRes)?.mutate()
            drawable?.setTint(tvCategory.currentTextColor)
            tvCategory.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            tvCategory.compoundDrawablePadding = 8

            checkbox.setOnClickListener {
                onToggle(habit)
            }

            btnOptions.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                val editItem = popup.menu.add(0, 1, 0, "Edit")
                editItem.setIcon(R.drawable.ic_edit)
                val deleteItem = popup.menu.add(0, 2, 1, "Delete")
                deleteItem.setIcon(R.drawable.ic_delete)

                try {
                    val field = PopupMenu::class.java.getDeclaredField("mPopup")
                    field.isAccessible = true
                    val mPopup = field.get(popup)
                    mPopup?.javaClass?.getDeclaredMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)?.invoke(mPopup, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            onEdit(habit)
                            true
                        }
                        2 -> {
                            onDelete(habit)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HabitEntity>() {
        override fun areItemsTheSame(oldItem: HabitEntity, newItem: HabitEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HabitEntity, newItem: HabitEntity): Boolean = oldItem == newItem
    }
}
