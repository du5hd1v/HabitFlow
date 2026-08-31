package com.example.habitflow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitflow.R
import com.example.habitflow.data.remote.GeneratedTaskItem

class AiStepsAdapter(
    private val onAddStep: (GeneratedTaskItem) -> Unit
) : ListAdapter<GeneratedTaskItem, AiStepsAdapter.StepViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ai_step, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_step_title)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_step_time)
        private val btnAdd: Button = itemView.findViewById(R.id.btn_add_single_step)

        fun bind(item: GeneratedTaskItem) {
            tvTitle.text = item.title
            tvTime.text = "Estimated: ${item.estimatedMins} mins"
            btnAdd.setOnClickListener {
                onAddStep(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GeneratedTaskItem>() {
        override fun areItemsTheSame(oldItem: GeneratedTaskItem, newItem: GeneratedTaskItem): Boolean = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: GeneratedTaskItem, newItem: GeneratedTaskItem): Boolean = oldItem == newItem
    }
}
