package com.example.habittracker.HabitEntry

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.habittracker.Habit.HabitEntity
import java.util.UUID

@Entity(
    tableName = "habit_entry",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["habitId"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index(value = ["habitId"])]
)
data class HabitEntryEntity(
    @PrimaryKey
    val entryId: String,
    val habitId: String,
    val timeStamp: Long,
    val entryNote: String? = null
)