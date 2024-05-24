package com.tajayajaya.sportracjaya.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tajayajaya.sportracjaya.R
import com.tajayajaya.sportracjaya.ui.fragments.ScheduleFragment

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm received")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel if necessary (required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("exercise_reminder", "Exercise Reminder", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for exercise reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val scheduleId = intent.getIntExtra("scheduleId", -1)
        val notificationIntent = Intent(context, ScheduleFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(context, "exercise_reminder")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Waktunya Berolahraga!")
            .setContentText("Ingat untuk berolahraga sekarang.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(scheduleId, notification)
    }
}