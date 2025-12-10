package com.example.habittracker.Habit

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit")
data class HabitEntity(
    @PrimaryKey
    val habitId: String,
    val description: String,
    val frequency: Int,
    val freqGroup: String
)
