package com.example.habittracker.HabitEntry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEntryDao {

    @Insert
    suspend fun insertHabitEntry(habitEntry: HabitEntryEntity)

    @Delete
    suspend fun deleteHabitEntry(habitEntry: HabitEntryEntity)

    @Update
    suspend fun updateHabitEntry(habitEntry: HabitEntryEntity)

    @Query("SELECT * FROM habit_entry WHERE habitId = :habitId")
    fun getHabitEntriesByHabit(habitId: String): Flow<List<HabitEntryEntity>>

    @Query("SELECT * FROM habit_entry WHERE timeStamp BETWEEN :startTmp AND :endTmp")
    fun getEntriesByDateRange(startTmp: Long, endTmp: Long) : Flow<List<HabitEntryEntity>>

    @Query("DELETE FROM habit_entry WHERE habitId = :habitId AND timeStamp BETWEEN :startD AND :endD")
    suspend fun deleteEntryByHabitIdAndDayRange(habitId: String, startD: Long,endD: Long)


}