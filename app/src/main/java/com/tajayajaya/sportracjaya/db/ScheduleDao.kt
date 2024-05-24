package com.tajayajaya.sportracjaya.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insert(schedule: Schedule)

    @Query("SELECT * FROM schedule_table ORDER BY id ASC")
    fun getAllSchedules(): LiveData<List<Schedule>>

    @Query("DELETE FROM schedule_table WHERE id = :scheduleId")
    suspend fun delete(scheduleId: Int)

    @Query("UPDATE schedule_table SET hour = :hour, minute = :minute WHERE id = :scheduleId")
    suspend fun update(scheduleId: Int, hour: Int, minute: Int)
}