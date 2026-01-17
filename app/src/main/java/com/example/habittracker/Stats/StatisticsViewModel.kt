package com.example.habittracker.Stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.Habit.HabitRepo
import com.example.habittracker.HabitEntry.HabitEntryRepo
import com.example.habittracker.utils.getDayRange
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

class StatisticsViewModel(
    private val habitRepo: HabitRepo,
    private val entryRepo: HabitEntryRepo
) : ViewModel() {

    val statistics: StateFlow<List<HabitStatisticUi>> = combine(
        habitRepo.getAllHabits(),
        entryRepo.getAllEntries()
    ) { habits, entries ->
        habits.map { habit ->
            val habitEntries = entries.filter { it.habitId == habit.habitId }

            val streak = calculateStreak(habitEntries.map { it.timeStamp })
            val rate30Days = calculateCompletionRate(habitEntries.map { it.timeStamp }, 30)

            HabitStatisticUi(
                habit = habit,
                streak = streak,
                completionRate30Days = rate30Days,
                totalCompletions = habitEntries.size
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        val dates = timestamps.map {
            java.time.Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.distinct().sortedDescending()

        if (dates.isEmpty()) return 0

        var streak = 0
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        if (!dates.contains(today) && !dates.contains(yesterday)) {
            return 0
        }

        var checkDate = if (dates.contains(today)) today else yesterday

        while (dates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }

    private fun calculateCompletionRate(timestamps: List<Long>, daysBack: Int): Int {
        val today = LocalDate.now()
        val startDate = today.minusDays(daysBack.toLong())
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val entriesInPeriod = timestamps.count { it >= startMillis }
        return ((entriesInPeriod.toFloat() / daysBack) * 100).toInt().coerceIn(0, 100)
    }
}