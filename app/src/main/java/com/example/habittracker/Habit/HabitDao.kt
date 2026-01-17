package com.example.habittracker.Habit

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert
    suspend fun insertHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("SELECT * FROM habit")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit WHERE habitId = :habitId")
    fun getHabitById(habitId: String): Flow<HabitEntity>
}