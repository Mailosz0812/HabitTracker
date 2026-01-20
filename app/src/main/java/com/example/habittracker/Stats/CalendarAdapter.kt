package com.example.habittracker.Stats

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import java.time.LocalDate

data class DayStatus(
    val date: LocalDate?,
    val count: Int
)

class CalendarAdapter(private var days: List<DayStatus>) : RecyclerView.Adapter<CalendarAdapter.DayHolder>() {

    fun submitList(newDays: List<DayStatus>) {
        days = newDays
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayHolder(view)
    }

    override fun onBindViewHolder(holder: DayHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount() = days.size

    class DayHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bgView: View = itemView.findViewById(R.id.day_background)
        private val dayText: TextView = itemView.findViewById(R.id.day_text)

        fun bind(status: DayStatus) {
            if (status.date == null) {
                bgView.visibility = View.INVISIBLE
                dayText.visibility = View.INVISIBLE
                return
            }
            bgView.visibility = View.VISIBLE
            dayText.visibility = View.VISIBLE
            dayText.text = status.date.dayOfMonth.toString()

            val backgroundColor = when {
                status.count == 0 -> Color.parseColor("#EEEEEE")
                status.count == 1 -> Color.parseColor("#66BB6A")
                status.count == 2 -> Color.parseColor("#43A047")
                else -> Color.parseColor("#1B5E20")
            }
            bgView.backgroundTintList = ColorStateList.valueOf(backgroundColor)

            if (status.count == 0) {
                dayText.setTextColor(Color.parseColor("#757575"))
            } else {
                dayText.setTextColor(Color.WHITE)
            }

            if (status.date == LocalDate.now()) {
                dayText.paint.isFakeBoldText = true
            } else {
                dayText.paint.isFakeBoldText = false
            }
        }
    }
}