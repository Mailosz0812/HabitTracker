package com.example.habittracker

import android.app.Application
import com.example.habittracker.Habit.HabitMapper
import com.example.habittracker.Habit.HabitRepo
import com.example.habittracker.HabitEntry.HabitEntryMapper
import com.example.habittracker.HabitEntry.HabitEntryRepo

class HabitApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }

    val entryRepo by lazy {
        HabitEntryRepo(
            habitDao = database.habitDao(),
            habitMapper = HabitMapper(),
            habitEntryDao = database.habitEntryDao(),
            habitEntryMapper = HabitEntryMapper()
        )
    }
    val habitRepo by lazy {
        HabitRepo(
            habitDao = database.habitDao(),
            habitMapper = HabitMapper(),
            habitEntryDao = database.habitEntryDao(),
            habitEntryMapper = HabitEntryMapper()
        )
    }
}