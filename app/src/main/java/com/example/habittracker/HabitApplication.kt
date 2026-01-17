package com.example.habittracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.habittracker.Habit.HabitMapper
import com.example.habittracker.Habit.HabitRepo
import com.example.habittracker.HabitEntry.HabitEntryMapper
import com.example.habittracker.HabitEntry.HabitEntryRepo

class HabitApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }

    val entryRepo by lazy {
        HabitEntryRepo(
            habitDao = database.habitDao(),
            habitMapper = HabitMapper(),
            habitEntryDao = database.habitEntryDao(),
            habitEntryMapper = HabitEntryMapper()
        )
    }

    val habitRepo by lazy {
        HabitRepo(
            habitDao = database.habitDao(),
            habitMapper = HabitMapper(),
            habitEntryDao = database.habitEntryDao(),
            habitEntryMapper = HabitEntryMapper()
        )
    }

    companion object {
        const val CHANNEL_ID = "habit_reminder_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Daily reminders to complete your habits"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}