package com.example.habittracker.Habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.HabitEntry.HabitEntryRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitListViewModel(private val entryRepo: HabitEntryRepo,private val habitRepo: HabitRepo) : ViewModel() {

    val habits: StateFlow<List<HabitItemUi>> = entryRepo.getHabitsForToday()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onCheckedChanged(habit: Habit, isChecked : Boolean){
        if(isChecked){
            viewModelScope.launch {
                entryRepo.addEntry(habit)
            }
        }else{
            viewModelScope.launch {
                entryRepo.deleteEntry(habit)
            }
        }

    }
     fun addHabit(habit: Habit){
         viewModelScope.launch {
             habitRepo.addHabit(habit)
         }
    }


}