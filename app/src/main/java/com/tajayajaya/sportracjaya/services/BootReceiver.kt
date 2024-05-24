package com.tajayajaya.sportracjaya.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tajayajaya.sportracjaya.db.Schedule
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sharedPref = context.getSharedPreferences("schedules", Context.MODE_PRIVATE)
            val allEntries = sharedPref.all
            for ((key, value) in allEntries) {
                if (key.startsWith("hour_")) {
                    val id = key.removePrefix("hour_").toInt()
                    val hour = value as Int
                    val minute = sharedPref.getInt("minute_$id", 0)
                    val schedule = Schedule(id = id, hour = hour, minute = minute)
                    Log.d("BootReceiver", "Resetting alarm for schedule: $schedule")
                    setAlarm(context, schedule)
                }
            }
        }
    }

    private fun setAlarm(context: Context, schedule: Schedule) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
        }
        Log.d("BootReceiver", "Alarm set for: ${calendar.time}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("scheduleId", schedule.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}