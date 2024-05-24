package com.tajayajaya.sportracjaya.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.tajayajaya.sportracjaya.db.Schedule
import com.tajayajaya.sportracjaya.db.ScheduleDatabase
import com.tajayajaya.sportracjaya.repositories.ScheduleRepository
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ScheduleRepository
    val allSchedules: LiveData<List<Schedule>>

    init {
        val scheduleDao = ScheduleDatabase.getDatabase(application).scheduleDao()
        repository = ScheduleRepository(scheduleDao)
        allSchedules = repository.allSchedules
    }

    fun insert(schedule: Schedule) = viewModelScope.launch {
        repository.insert(schedule)
    }

    fun delete(scheduleId: Int) = viewModelScope.launch {
        repository.delete(scheduleId)
    }

    fun update(scheduleId: Int, hour: Int, minute: Int) = viewModelScope.launch {
        repository.update(scheduleId, hour, minute)
    }
}