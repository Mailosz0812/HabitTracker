package com.example.habittracker.Stats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R

class StatisticsAdapter(
    private val onItemClick: (HabitStatisticUi) -> Unit
) : ListAdapter<HabitStatisticUi, StatisticsAdapter.StatsHolder>(StatsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit_statistic, parent, false)
        // Przekazujemy onItemClick do Holdera
        return StatsHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: StatsHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StatsHolder(
        itemView: View,
        private val onItemClick: (HabitStatisticUi) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.stat_habit_name)
        private val streakTv: TextView = itemView.findViewById(R.id.stat_streak_value)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.stat_progress_bar)
        private val totalTv: TextView = itemView.findViewById(R.id.stat_total_count)

        fun bind(stat: HabitStatisticUi) {
            nameTv.text = stat.habit.name

            val dayLabel = if (stat.streak == 1) "day" else "days"
            streakTv.text = "${stat.streak} $dayLabel"

            if (stat.streak > 0) {
                streakTv.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            } else {
                streakTv.setTextColor(android.graphics.Color.GRAY)
            }

            progressBar.progress = stat.completionRate30Days
            if (stat.completionRate30Days >= 80) {
                progressBar.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")) // Zielony
            } else if (stat.completionRate30Days >= 50) {
                progressBar.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFC107")) // Żółty
            } else {
                progressBar.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336")) // Czerwony
            }

            totalTv.text = "Total completions: ${stat.totalCompletions}"

            itemView.setOnClickListener {
                onItemClick(stat)
            }
        }
    }

    private object StatsDiffCallback : DiffUtil.ItemCallback<HabitStatisticUi>() {
        override fun areItemsTheSame(oldItem: HabitStatisticUi, newItem: HabitStatisticUi): Boolean {
            return oldItem.habit.habitId == newItem.habit.habitId
        }
        override fun areContentsTheSame(oldItem: HabitStatisticUi, newItem: HabitStatisticUi): Boolean {
            return oldItem == newItem
        }
    }
}