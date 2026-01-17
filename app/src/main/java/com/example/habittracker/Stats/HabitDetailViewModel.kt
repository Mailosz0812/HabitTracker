package com.example.habittracker.Stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.Habit.Habit
import com.example.habittracker.Habit.HabitRepo
import com.example.habittracker.HabitEntry.HabitEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

// Definicja stanu UI
data class HabitDetailState(
    val habit: Habit? = null,
    val entries: List<HabitEntry> = emptyList(),
    val bestStreak: Int = 0,
    val bestDayOfWeek: String = "-"
)

class HabitDetailViewModel(
    private val habitId: String,
    private val habitRepo: HabitRepo
) : ViewModel() {

    // Używamy combine, aby połączyć dane o nawyku i jego wpisach
    val uiState: StateFlow<HabitDetailState> = combine(
        habitRepo.getHabitById(habitId),      // Funkcja 1
        habitRepo.getEntriesForHabit(habitId) // Funkcja 2
    ) { habit, entries ->
        val timestamps = entries.map { it.timeStamp }

        // Obliczenia
        val bestStreak = calculateBestStreak(timestamps)
        val bestDay = calculateBestDayOfWeek(timestamps)

        HabitDetailState(
            habit = habit,
            entries = entries,
            bestStreak = bestStreak,
            bestDayOfWeek = bestDay
        )
    }
        .catch { emit(HabitDetailState()) } // Zabezpieczenie przed błędami (np. brak nawyku)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HabitDetailState()
        )

    private fun calculateBestStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

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
                    currentStreak++
                } else {
                    if (currentStreak > maxStreak) maxStreak = currentStreak
                    currentStreak = 1
                }
            }
            previousDate = date
        }
        if (currentStreak > maxStreak) maxStreak = currentStreak

        return maxStreak
    }

    private fun calculateBestDayOfWeek(timestamps: List<Long>): String {
        if (timestamps.isEmpty()) return "Brak danych"

        val zoneId = ZoneId.systemDefault()
        val dayCounts = timestamps.map {
            java.time.Instant.ofEpochMilli(it).atZone(zoneId).dayOfWeek
        }.groupingBy { it }.eachCount()

        val bestDay = dayCounts.maxByOrNull { it.value }?.key ?: return "Brak danych"

        return bestDay.getDisplayName(TextStyle.FULL, Locale("pl", "PL"))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}