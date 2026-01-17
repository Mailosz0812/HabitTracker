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

            val color = when {
                status.count == 0 -> Color.parseColor("#E0E0E0") // Szary (brak)
                status.count == 1 -> Color.parseColor("#A5D6A7") // Jasny zielony
                status.count == 2 -> Color.parseColor("#4CAF50") // Średni
                else -> Color.parseColor("#2E7D32")             // Ciemny (dużo)
            }
            bgView.backgroundTintList = ColorStateList.valueOf(color)

            if (status.date == LocalDate.now()) {
                dayText.setTextColor(Color.BLACK)
                dayText.paint.isFakeBoldText = true
            } else {
                dayText.setTextColor(Color.parseColor("#757575"))
            }
        }
    }
}