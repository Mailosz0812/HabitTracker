package com.example.habittracker.Stats

import com.example.habittracker.Habit.Habit

data class HabitStatisticUi(
    val habit: Habit,
    val streak: Int,
    val completionRate30Days: Int,
    val totalCompletions: Int
)
