package com.example.habittracker.Stats

import com.example.habittracker.Habit.Habit

data class HabitStatisticUi(
    val habit: Habit,
    val streak: Int,
    val longestStreak: Int,        // NOWE POLE: Rekord wszechczasów
    val completionRate: Int,
    val daysCompletedInMonth: Int,
    val daysPassedInMonth: Int,
    val totalCompletions: Int
)