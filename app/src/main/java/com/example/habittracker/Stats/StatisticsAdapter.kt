package com.example.habittracker.Stats

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
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


        private val percentTv: TextView = itemView.findViewById(R.id.stat_percent_text)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.stat_progress_bar)
        private val countTv: TextView = itemView.findViewById(R.id.stat_count_text)

        fun bind(stat: HabitStatisticUi) {
            nameTv.text = stat.habit.name

            val context = itemView.context
            val dayLabel = if (stat.streak == 1)
                context.getString(R.string.day_singular)
            else
                context.getString(R.string.day_plural)

            streakValueTv.text = "${stat.streak} $dayLabel"

            if (stat.streak > 0) {
                val orange = ContextCompat.getColor(context,R.color.habit_streak_orange)
                streakValueTv.setTextColor(orange)
                streakLabelTv.setTextColor(orange)
            } else {
                streakValueTv.setTextColor(Color.GRAY)
                streakLabelTv.setTextColor(Color.GRAY)
            }

            val percentage = stat.completionRate
            progressBar.progress = percentage
            percentTv.text = "$percentage%"

            val color = when {
                percentage >= 80 -> ContextCompat.getColor(context,R.color.habit_done_green)
                percentage >= 50 -> ContextCompat.getColor(context,R.color.percentage_50)
                else -> ContextCompat.getColor(context,R.color.habit_delete_red)
            }

            progressBar.progressTintList = ColorStateList.valueOf(color)
            percentTv.setTextColor(color)

            countTv.text = context.getString(
                R.string.regularity_format,
                stat.daysCompletedInMonth,
                stat.daysPassedInMonth
            )

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