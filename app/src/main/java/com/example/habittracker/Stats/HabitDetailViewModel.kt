package com.example.habittracker.Stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.Habit.Habit
import com.example.habittracker.Habit.HabitRepo
import com.example.habittracker.HabitEntry.HabitEntry
import kotlinx.coroutines.flow.*
import java.util.UUID

data class HabitDetailState(
    val habit: Habit? = null,
    val entries: List<HabitEntry> = emptyList()
)

class HabitDetailViewModel(
    private val habitId: String,
    private val habitRepo: HabitRepo
) : ViewModel() {

    val uiState: StateFlow<HabitDetailState> = combine(
        habitRepo.getAllHabits(),
        habitRepo.getHabitEntriesByHabitId(UUID.fromString(habitId))
    ) { habits, entries ->
        val selectedHabit = habits.find { it.habitId.toString() == habitId }
        HabitDetailState(selectedHabit, entries)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitDetailState()
    )
}