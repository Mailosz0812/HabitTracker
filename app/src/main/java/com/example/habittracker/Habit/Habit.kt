package com.example.habittracker.Habit

import java.util.UUID

data class Habit(
    val habitId: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val frequency: Int,
    val freqGroup: FreqGroup,
    val color: Int
)