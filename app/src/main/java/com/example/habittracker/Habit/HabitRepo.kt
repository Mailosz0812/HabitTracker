package com.example.habittracker.Habit

import com.example.habittracker.HabitEntry.HabitEntry
import com.example.habittracker.HabitEntry.HabitEntryDao
import com.example.habittracker.HabitEntry.HabitEntryMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class HabitRepo(private val habitDao: HabitDao,private val habitMapper: HabitMapper
    ,private val habitEntryDao: HabitEntryDao, private val habitEntryMapper: HabitEntryMapper
)
{
    suspend fun addHabit(habit: Habit){
        val habitEntity: HabitEntity = habitMapper.toEntity(habit)

        habitDao.insertHabit(habitEntity)
    }

    suspend fun deleteHabit(habit: Habit){
        val habitEntity: HabitEntity = habitMapper.toEntity(habit)

        habitDao.deleteHabit(habitEntity)
    }

    fun getHabitEntriesByHabitId(habitId: UUID): Flow<List<HabitEntry>> {
        val idAsString: String = habitId.toString()

        return habitEntryDao.getHabitEntriesByHabit(idAsString)
            .map{
                list ->
                list.map {entryEntity -> habitEntryMapper.toDomain(entryEntity)}

            }
    }
    fun getAllHabits(): Flow<List<Habit>>{
        return habitDao.getAllHabits()
            .map { list ->
                list.map { habitMapper.toDomain(it)}
            }
    }





}