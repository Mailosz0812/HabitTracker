package com.example.habittracker.Stats

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.HabitApplication
import com.example.habittracker.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import androidx.navigation.fragment.findNavController

class HabitDetailFragment : Fragment(R.layout.fragment_habit_detail) {

    private lateinit var viewModel: HabitDetailViewModel
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val habitId = arguments?.getString("habitId") ?: return

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as HabitApplication
                return HabitDetailViewModel(habitId, app.habitRepo) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[HabitDetailViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.close_button).setOnClickListener {
            findNavController().navigateUp()
        }

        val calendarRecycler = view.findViewById<RecyclerView>(R.id.calendar_recycler_view)
        calendarAdapter = CalendarAdapter(emptyList())
        calendarRecycler.adapter = calendarAdapter
        calendarRecycler.layoutManager = GridLayoutManager(context, 7)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.habit != null) {
                        updateUI(view, state)
                    }
                }
            }
        }
    }

    private fun updateUI(view: View, state: HabitDetailState) {
        view.findViewById<TextView>(R.id.detail_title).text = state.habit?.name
        view.findViewById<TextView>(R.id.detail_desc).text = state.habit?.description

        view.findViewById<TextView>(R.id.calendar_month_name).text =
            "${YearMonth.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${YearMonth.now().year}"

        val timestamps = state.entries.map { it.timeStamp }

        updateMonthlyProgress(view, timestamps)

        updateCalendar(timestamps)

        updateBarChart(view, timestamps)
    }

    private fun updateMonthlyProgress(view: View, timestamps: List<Long>) {
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()

        val startOfMonth = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val currentMonthEntries = timestamps.filter { it in startOfMonth until endOfMonth }

        val activeDaysCount = currentMonthEntries.map { ts ->
            java.time.Instant.ofEpochMilli(ts).atZone(zoneId).toLocalDate()
        }.distinct().size

        val daysPassed = today.dayOfMonth

        val percentage = if (daysPassed > 0) {
            (activeDaysCount.toFloat() / daysPassed * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        view.findViewById<TextView>(R.id.month_percent_text).text = "$percentage%"
        val progressBar = view.findViewById<ProgressBar>(R.id.month_progress_bar)
        progressBar.progress = percentage

        val color = when {
            percentage >= 80 -> 0xFF4CAF50.toInt() // Zielony (Świetnie)
            percentage >= 50 -> 0xFFFFC107.toInt() // Żółty (Średnio)
            else -> 0xFFF44336.toInt()             // Czerwony (Słabo)
        }
        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)

        view.findViewById<TextView>(R.id.month_count_text).text = "Regularność: $activeDaysCount / $daysPassed dni"
    }

    private fun updateCalendar(timestamps: List<Long>) {
        val yearMonth = YearMonth.now()
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value

        val dayList = mutableListOf<DayStatus>()

        for (i in 1 until firstDayOfWeek) {
            dayList.add(DayStatus(null, 0))
        }

        for (day in 1..daysInMonth) {
            val date = LocalDate.of(yearMonth.year, yearMonth.month, day)
            val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val count = timestamps.count { it in dayStart until dayEnd }
            dayList.add(DayStatus(date, count))
        }
        calendarAdapter.submitList(dayList)
    }

    private fun updateBarChart(view: View, allTimestamps: List<Long>) {
        val container = view.findViewById<LinearLayout>(R.id.chart_container)
        container.removeAllViews()

        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        val startOfMonth = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val currentMonthTimestamps = allTimestamps.filter { it in startOfMonth until endOfMonth }

        val daysCounts = IntArray(7)
        currentMonthTimestamps.forEach { ts ->
            val date = java.time.Instant.ofEpochMilli(ts).atZone(zoneId).toLocalDate()
            val dayOfWeekIndex = date.dayOfWeek.value - 1
            daysCounts[dayOfWeekIndex]++
        }

        val maxCount = (daysCounts.maxOrNull() ?: 1).coerceAtLeast(1)
        val daysLabels = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

        for (i in 0..6) {
            val count = daysCounts[i]

            val dayColumnLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            }

            val countText = TextView(context).apply {
                text = count.toString()
                textSize = 12f
                setTextColor(android.graphics.Color.GRAY)
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 4)
                }
                visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
            }

            val maxBarHeightDp = 120
            val density = resources.displayMetrics.density
            val barHeightPixels = if (maxCount > 0) {
                ((count.toFloat() / maxCount) * maxBarHeightDp * density).toInt()
            } else 0

            val finalBarHeight = if (count > 0) barHeightPixels.coerceAtLeast((10 * density).toInt()) else (2 * density).toInt()

            val barView = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (16 * density).toInt(),
                    finalBarHeight
                )
                setBackgroundResource(R.drawable.bg_bar_rounded)

                val color = if (count > 0) 0xFF4CAF50.toInt() else 0xFFE0E0E0.toInt()
                backgroundTintList = android.content.res.ColorStateList.valueOf(color)
            }

            val dayLabel = TextView(context).apply {
                text = daysLabels[i]
                textSize = 12f
                setTextColor(android.graphics.Color.BLACK)
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 0)
                }

                if (i == (LocalDate.now().dayOfWeek.value - 1)) {
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    setTextColor(0xFF4CAF50.toInt())
                }
            }

            dayColumnLayout.addView(countText)
            dayColumnLayout.addView(barView)
            dayColumnLayout.addView(dayLabel)

            container.addView(dayColumnLayout)
        }
    }
}