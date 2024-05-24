package com.tajayajaya.sportracjaya.ui.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tajayajaya.sportracjaya.R
import com.tajayajaya.sportracjaya.adapters.ScheduleAdapter
import com.tajayajaya.sportracjaya.db.Schedule
import com.tajayajaya.sportracjaya.services.AlarmReceiver
import com.tajayajaya.sportracjaya.viewmodels.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private lateinit var etTime: EditText
    private lateinit var btnSetSchedule: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTime = view.findViewById(R.id.et_time)
        btnSetSchedule = view.findViewById(R.id.btn_set_schedule)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_schedules)
        val adapter = ScheduleAdapter(this, scheduleViewModel)  // Pass the fragment reference here
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        scheduleViewModel.allSchedules.observe(viewLifecycleOwner, Observer { schedules ->
            schedules?.let { adapter.setSchedules(it) }
        })

        btnSetSchedule.setOnClickListener {
            val time = etTime.text.toString()
            if (time.isNotEmpty()) {
                val timeParts = time.split(":")
                if (timeParts.size == 2) {
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()
                    val schedule = Schedule(hour = hour, minute = minute)
                    scheduleViewModel.insert(schedule)
                    setAlarm(schedule)
                } else {
                    Toast.makeText(requireContext(), "Format waktu tidak valid", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Masukkan waktu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveScheduleToPreferences(schedule: Schedule) {
        val sharedPref = requireContext().getSharedPreferences("schedules", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("hour_${schedule.id}", schedule.hour)
            putInt("minute_${schedule.id}", schedule.minute)
            apply()
        }
    }

    fun removeScheduleFromPreferences(scheduleId: Int) {
        val sharedPref = requireContext().getSharedPreferences("schedules", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("hour_$scheduleId")
            remove("minute_$scheduleId")
            apply()
        }
    }

    fun setAlarm(schedule: Schedule) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed for today, schedule it for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        Log.d("ScheduleFragment", "Setting alarm for: ${calendar.time}")
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("scheduleId", schedule.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            schedule.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }

        saveScheduleToPreferences(schedule)
        Toast.makeText(requireContext(), "Jadwal olahraga telah diset", Toast.LENGTH_SHORT).show()
    }

    fun cancelAlarm(schedule: Schedule) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            schedule.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        removeScheduleFromPreferences(schedule.id)
        Log.d("ScheduleFragment", "Alarm canceled for: ${schedule.id}")
        Toast.makeText(requireContext(), "Jadwal olahraga telah dibatalkan", Toast.LENGTH_SHORT).show()
    }

    fun checkAndRequestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }
}
