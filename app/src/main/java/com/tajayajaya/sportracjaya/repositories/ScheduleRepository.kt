package com.tajayajaya.sportracjaya.repositories

import androidx.lifecycle.LiveData
import com.tajayajaya.sportracjaya.db.Schedule
import com.tajayajaya.sportracjaya.db.ScheduleDao

class ScheduleRepository(private val scheduleDao: ScheduleDao) {
    val allSchedules: LiveData<List<Schedule>> = scheduleDao.getAllSchedules()

    suspend fun insert(schedule: Schedule) {
        scheduleDao.insert(schedule)
    }

    suspend fun delete(scheduleId: Int) {
        scheduleDao.delete(scheduleId)
    }

    suspend fun update(scheduleId: Int, hour: Int, minute: Int) {
        scheduleDao.update(scheduleId, hour, minute)
    }
}