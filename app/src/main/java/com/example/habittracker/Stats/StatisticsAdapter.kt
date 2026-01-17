package com.example.habittracker.Stats

import android.content.res.ColorStateList
import android.graphics.Color
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
        private val streakValueTv: TextView = itemView.findViewById(R.id.stat_streak_value)
        private val streakLabelTv: TextView = itemView.findViewById(R.id.stat_streak_label)

        // USUNIĘTO: private val bestStreakTv

        private val percentTv: TextView = itemView.findViewById(R.id.stat_percent_text)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.stat_progress_bar)
        private val countTv: TextView = itemView.findViewById(R.id.stat_count_text)

        fun bind(stat: HabitStatisticUi) {
            nameTv.text = stat.habit.name

            // Aktualny Streak
            val dayLabel = if (stat.streak == 1) "dzień" else "dni"
            streakValueTv.text = "${stat.streak} $dayLabel"

            if (stat.streak > 0) {
                val orange = Color.parseColor("#FF9800")
                streakValueTv.setTextColor(orange)
                streakLabelTv.setTextColor(orange)
            } else {
                streakValueTv.setTextColor(Color.GRAY)
                streakLabelTv.setTextColor(Color.GRAY)
            }

            // USUNIĘTO blok kodu obsługujący bestStreakTv

            // Pasek postępu
            val percentage = stat.completionRate
            progressBar.progress = percentage
            percentTv.text = "$percentage%"

            val color = when {
                percentage >= 80 -> Color.parseColor("#4CAF50")
                percentage >= 50 -> Color.parseColor("#FFC107")
                else -> Color.parseColor("#F44336")
            }

            progressBar.progressTintList = ColorStateList.valueOf(color)
            percentTv.setTextColor(color)

            countTv.text = "Regularność: ${stat.daysCompletedInMonth} / ${stat.daysPassedInMonth} dni"

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