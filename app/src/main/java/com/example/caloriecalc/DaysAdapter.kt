package com.example.caloriecalc

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.data.CalendarDay
import android.view.ViewGroup
import java.time.format.DateTimeFormatter
import java.util.Locale


class DaysAdapter(
    private val onDaySelected: (CalendarDay) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    private val daysList = mutableListOf<CalendarDay>()
    private var selectedPosition = -1

    fun submitList(newDays: List<CalendarDay>) {
        daysList.clear()
        daysList.addAll(newDays)

        // Снимаем выделение с сегодняшнего дня, если оно есть
        val todayPosition = newDays.indexOfFirst { it.isToday }
        if (todayPosition != -1 && selectedPosition == todayPosition) {
            selectedPosition = -1
        }

        // Найдем позицию сегодняшнего дня и выделим его по умолчанию
        selectedPosition = newDays.indexOfFirst { it.isToday }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_calendar_day, parent, false
        )
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(daysList[position], position)
    }

    override fun getItemCount(): Int = daysList.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.dayText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)

        private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale("ru"))

        fun bind(day: CalendarDay, position: Int) {
            dayText.text = day.date.format(dayOfWeekFormatter) // "ПН", "ВТ", и т.д.
            dateText.text = day.date.dayOfMonth.toString()

            // Выделение выбранного дня
            itemView.isSelected = position == selectedPosition || day.isToday
            itemView.background = itemView.context.getDrawable(
                if (position == selectedPosition) R.drawable.calendar_day_background
                else R.drawable.calendar_day_background_default
            )

            itemView.setOnClickListener {
                // Снимаем выделение с предыдущего дня
                val previousSelected = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousSelected) // Снимаем выделение с предыдущего дня
                notifyItemChanged(position) // Выделяем новый день
                onDaySelected(day)
            }
        }
    }
}
