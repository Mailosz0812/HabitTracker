package com.example.habittracker.Stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.Habit.HabitRepo
import com.example.habittracker.HabitEntry.HabitEntryRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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
            val timestamps = habitEntries.map { it.timeStamp }

            val streak = calculateStreak(timestamps)
            val longestStreak = calculateLongestStreak(timestamps) // Obliczamy rekord

            val (rate, completed, passed) = calculateMonthlyStats(timestamps)

            HabitStatisticUi(
                habit = habit,
                streak = streak,
                longestStreak = longestStreak, // Przekazujemy rekord
                completionRate = rate,
                daysCompletedInMonth = completed,
                daysPassedInMonth = passed,
                totalCompletions = habitEntries.size
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Logika aktualnej serii (bez zmian)
    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0
        val dates = timestamps.map {
            java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }.distinct().sortedDescending()
        if (dates.isEmpty()) return 0

        var streak = 0
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        if (!dates.contains(today) && !dates.contains(yesterday)) return 0

        var checkDate = if (dates.contains(today)) today else yesterday
        while (dates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    // Logika najdłuższej serii (NOWA)
    private fun calculateLongestStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        // Sortujemy daty rosnąco (od najstarszej)
        val dates = timestamps.map {
            java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }.distinct().sorted()

        var maxStreak = 0
        var currentStreak = 0
        var previousDate: LocalDate? = null

        for (date in dates) {
            if (previousDate == null) {
                currentStreak = 1
            } else {
                val daysBetween = ChronoUnit.DAYS.between(previousDate, date)
                if (daysBetween == 1L) {
                    // Kontynuacja serii
                    currentStreak++
                } else {
                    // Przerwa w serii
                    if (currentStreak > maxStreak) maxStreak = currentStreak
                    currentStreak = 1
                }
            }
            previousDate = date
        }
        // Sprawdzenie ostatniej serii po zakończeniu pętli
        if (currentStreak > maxStreak) maxStreak = currentStreak

        return maxStreak
    }

    private fun calculateMonthlyStats(timestamps: List<Long>): Triple<Int, Int, Int> {
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        val startOfMonth = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val daysPassed = today.dayOfMonth
        val completedInMonth = timestamps
            .filter { it >= startOfMonth }
            .map { java.time.Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
            .distinct()
            .size
        val rate = if (daysPassed > 0) {
            ((completedInMonth.toFloat() / daysPassed) * 100).toInt().coerceIn(0, 100)
        } else 0
        return Triple(rate, completedInMonth, daysPassed)
    }
}