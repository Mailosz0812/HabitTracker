package com.example.habittracker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val json = URL("https://zenquotes.io/api/today").readText()
                val type = object : TypeToken<List<Quote>>() {}.type
                val quotes: List<Quote> = Gson().fromJson(json, type)
                val todayQuote = quotes.firstOrNull()


                val defaultMsg = applicationContext.getString(R.string.notif_default_msg)
                val defaultAuthor = applicationContext.getString(R.string.notif_default_author)
                val quoteText = todayQuote?.q ?: defaultMsg
                val quoteAuthor = todayQuote?.a ?: defaultAuthor

                showNotification(quoteText, quoteAuthor)

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                showNotification(
                    applicationContext.getString(R.string.notif_fallback_title),
                    applicationContext.getString(R.string.notif_fallback_msg)
                )
                Result.failure()
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val builder = NotificationCompat.Builder(applicationContext, HabitApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.notif_title_daily))
            .setContentText("\"$title\" - $message")
            .setStyle(NotificationCompat.BigTextStyle().bigText("\"$title\"\n- $message"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(applicationContext).notify(1, builder.build())
    }
}