package com.example.habittracker.Habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.habittracker.HabitEntry.HabitEntryRepo
import com.example.habittracker.Stats.StatisticsViewModel

class HabitViewModelFactory(private val entryRepo: HabitEntryRepo, private val habitRepo: HabitRepo ): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HabitListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitListViewModel(entryRepo,habitRepo) as T
        }

        if(modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(habitRepo, entryRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}