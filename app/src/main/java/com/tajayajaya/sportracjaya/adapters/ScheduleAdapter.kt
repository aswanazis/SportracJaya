package com.tajayajaya.sportracjaya.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tajayajaya.sportracjaya.R
import com.tajayajaya.sportracjaya.db.Schedule
import com.tajayajaya.sportracjaya.ui.fragments.EditScheduleDialogFragment
import com.tajayajaya.sportracjaya.ui.fragments.ScheduleFragment
import com.tajayajaya.sportracjaya.viewmodels.ScheduleViewModel

class ScheduleAdapter(
    private val fragment: ScheduleFragment,
    private val viewModel: ScheduleViewModel
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    private var schedules = emptyList<Schedule>()


    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text_view)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return ScheduleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val current = schedules[position]
        holder.textView.text = String.format("%02d:%02d", current.hour, current.minute)

        holder.btnEdit.setOnClickListener {
            val dialog = EditScheduleDialogFragment(current, viewModel)
            dialog.setTargetFragment(fragment, 0)
            dialog.show(fragment.parentFragmentManager, "EditScheduleDialog")
        }

        holder.btnDelete.setOnClickListener {
            (fragment as ScheduleFragment).cancelAlarm(current)
            viewModel.delete(current.id)
        }
    }

    override fun getItemCount() = schedules.size

    internal fun setSchedules(schedules: List<Schedule>) {
        this.schedules = schedules
        notifyDataSetChanged()
    }
}