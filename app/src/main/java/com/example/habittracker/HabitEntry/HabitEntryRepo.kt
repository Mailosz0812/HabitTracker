package com.example.habittracker.HabitEntry

import com.example.habittracker.Habit.Habit
import com.example.habittracker.Habit.HabitDao
import com.example.habittracker.Habit.HabitItemUi
import com.example.habittracker.Habit.HabitMapper
import com.example.habittracker.utils.getDayRange
import com.example.habittracker.utils.getNow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import kotlin.collections.map

class HabitEntryRepo(private val habitDao: HabitDao,private val habitMapper: HabitMapper
                     ,private val habitEntryDao: HabitEntryDao, private val habitEntryMapper: HabitEntryMapper
) {



    fun getHabitsForToday(): Flow<List<HabitItemUi>> {

        return combine(
            habitDao.getAllHabits(),
            getEntriesForToday()
        ){ allHabits, todayEntries ->
            allHabits.map { habitEntity ->
                val habit : Habit = habitMapper.toDomain(habitEntity)
                val isDone = todayEntries.any{ it.habitId == habit.habitId}
                HabitItemUi(
                    habit = habit,
                    isCompleted = isDone
                )
            }

        }
    }
    fun getEntriesForToday(): Flow<List<HabitEntry>>{
        val (start, end) = getDayRange(LocalDate.now())

        return habitEntryDao.getEntriesByDateRange(start,end).map { habitEntries ->
            habitEntries.map { habitEntryMapper.toDomain(it) }
        }
    }

    fun getAllEntries(): Flow<List<HabitEntry>> {
        return habitEntryDao.getAllEntries().map { list ->
            list.map { habitEntryMapper.toDomain(it) }
        }
    }

    suspend fun addEntry(habit: Habit){
        val entry = HabitEntry(
            habitId = habit.habitId,
            timeStamp = getNow(),
            entryNote = ""
        )
        habitEntryDao.insertHabitEntry(habitEntryMapper.toEntity(entry))
    }
    suspend fun deleteEntry(habit: Habit){
        val (start, end) = getDayRange(LocalDate.now())
        habitEntryDao.deleteEntryByHabitIdAndDayRange(habit.habitId.toString(),start,end)
    }
}