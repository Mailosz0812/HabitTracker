package com.example.habittracker.HabitEntry

import java.util.UUID

class HabitEntryMapper {

    fun toEntity(habitEntry: HabitEntry): HabitEntryEntity{
        return HabitEntryEntity(
            entryId = habitEntry.entryId.toString(),
            habitId = habitEntry.habitId.toString(),
            timeStamp = habitEntry.timeStamp,
            entryNote = habitEntry.entryNote
        )
    }

    fun toDomain(habitEntry: HabitEntryEntity): HabitEntry{
        return HabitEntry(
            entryId = UUID.fromString(habitEntry.entryId),
            habitId = UUID.fromString(habitEntry.habitId),
            timeStamp = habitEntry.timeStamp,
            entryNote = habitEntry.entryNote
        )
    }
}