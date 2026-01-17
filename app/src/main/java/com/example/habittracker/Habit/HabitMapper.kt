package com.example.habittracker.Habit

import java.util.UUID

class HabitMapper {

    fun toEntity(habit: Habit): HabitEntity{
        return HabitEntity(
            habitId = habit.habitId.toString(),
            description = habit.description,
            frequency = habit.frequency,
            freqGroup = habit.freqGroup.name,
            name = habit.name,
            color = habit.color
        )
    }

    fun toDomain(hEntity: HabitEntity): Habit{
        return Habit(
            habitId = UUID.fromString(hEntity.habitId),
            description = hEntity.description,
            frequency = hEntity.frequency,
            freqGroup = FreqGroup.valueOf(hEntity.freqGroup),
            name = hEntity.name,
            color = hEntity.color
        )
    }
}