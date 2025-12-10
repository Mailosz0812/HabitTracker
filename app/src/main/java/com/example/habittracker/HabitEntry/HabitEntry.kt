package com.example.habittracker.HabitEntry

import java.util.UUID

data class HabitEntry(
    val entryId: UUID,
    val habitId: UUID,
    val timeStamp: Long,
    val entryNote: String?
)
