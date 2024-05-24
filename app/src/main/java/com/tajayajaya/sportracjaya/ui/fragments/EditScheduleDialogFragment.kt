package com.tajayajaya.sportracjaya.ui.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.tajayajaya.sportracjaya.R
import com.tajayajaya.sportracjaya.db.Schedule
import com.tajayajaya.sportracjaya.viewmodels.ScheduleViewModel
import java.util.Calendar

class EditScheduleDialogFragment(
    private val schedule: Schedule,
    private val viewModel: ScheduleViewModel
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etHour = view.findViewById<EditText>(R.id.et_hour)
        val etMinute = view.findViewById<EditText>(R.id.et_minute)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        val calendar = Calendar.getInstance()
        etHour.setText(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)))
        etMinute.setText(String.format("%02d", calendar.get(Calendar.MINUTE)))

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                etHour.setText(String.format("%02d", hourOfDay))
                etMinute.setText(String.format("%02d", minute))
                Log.d("EditScheduleDialog", "Time picked: $hourOfDay:$minute")
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(requireContext())
        )

        etHour.setOnClickListener {
            Log.d("EditScheduleDialog", "etHour clicked")
            timePickerDialog.show()
        }

        etMinute.setOnClickListener {
            Log.d("EditScheduleDialog", "etMinute clicked")
            timePickerDialog.show()
        }

        btnSave.setOnClickListener {
            val newHour = etHour.text.toString().toInt()
            val newMinute = etMinute.text.toString().toInt()
            Log.d("EditScheduleDialog", "Saving new time: $newHour:$newMinute for schedule id: ${schedule.id}")
            viewModel.update(schedule.id, newHour, newMinute)
            Log.d("EditScheduleDialog", "Schedule updated to: $newHour:$newMinute")
            (targetFragment as? ScheduleFragment)?.setAlarm(Schedule(schedule.id, newHour, newMinute))
            Log.d("EditScheduleDialog", "Alarm reset for updated schedule")
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}