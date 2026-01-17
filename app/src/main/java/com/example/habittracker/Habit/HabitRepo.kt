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

    suspend fun updateHabit(habit: Habit){
        val habitEntity: HabitEntity = habitMapper.toEntity(habit)
        habitDao.updateHabit(habitEntity)
    }

    suspend fun deleteHabit(habit: Habit){
        val habitEntity: HabitEntity = habitMapper.toEntity(habit)
        habitDao.deleteHabit(habitEntity)
    }

    fun getHabitEntriesByHabitId(habitId: UUID): Flow<List<HabitEntry>> {
        val idAsString: String = habitId.toString()

        return habitEntryDao.getHabitEntriesByHabit(idAsString)
            .map{ list ->
                list.map {entryEntity -> habitEntryMapper.toDomain(entryEntity)}
            }
    }
    fun getAllHabits(): Flow<List<Habit>>{
        return habitDao.getAllHabits()
            .map { list ->
                list.map { habitMapper.toDomain(it)}
            }
    }

    fun getHabitById(habitId: String): Flow<Habit> {
        return habitDao.getHabitById(habitId)
            .map { entity ->
                // Zabezpieczenie na wypadek gdyby entity było null (np. przy usuwaniu)
                if (entity != null) habitMapper.toDomain(entity) else throw Exception("Habit not found")
            }
    }

    // 2. Pobieranie wpisów dla konkretnego nawyku (jako String)
    fun getEntriesForHabit(habitId: String): Flow<List<HabitEntry>> {
        return habitEntryDao.getHabitEntriesByHabit(habitId)
            .map{ list ->
                list.map { entryEntity -> habitEntryMapper.toDomain(entryEntity) }
            }
    }
}