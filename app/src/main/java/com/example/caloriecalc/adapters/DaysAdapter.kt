package com.example.caloriecalc.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.data.CalendarDay
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.caloriecalc.R
import com.example.caloriecalc.data.DiaryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DaysAdapter(
    private val onDaySelected: (CalendarDay) -> Unit,
    private val onDateSelected: (LocalDate) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    private val daysList = mutableListOf<CalendarDay>()
    var currentSelectedDate: LocalDate? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale("ru"))

    fun submitList(newDays: List<CalendarDay>) {
        daysList.clear()
        daysList.addAll(newDays)

        // Автоматически выделяем сегодняшний день при первой загрузке
        if (currentSelectedDate == null) {
            currentSelectedDate = newDays.firstOrNull { it.isToday }?.date
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }


    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = daysList[position]
        val isSelected = day.date == currentSelectedDate
        val isToday = day.isToday

        holder.bind(day, isSelected, isToday)
    }

    override fun getItemCount(): Int = daysList.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.dayText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)

        fun bind(day: CalendarDay, isSelected: Boolean, isToday: Boolean) {
            // Устанавливаем текст
            dayText.text = day.date.format(dayOfWeekFormatter) // "ПН", "ВТ" и т.д.
            dateText.text = day.date.dayOfMonth.toString()

            // Устанавливаем выделение
            itemView.isSelected = isSelected
            itemView.background = itemView.context.getDrawable(
                when {
                    isSelected -> R.drawable.calendar_day_background
                    isToday -> R.drawable.calendar_day_background_today
                    else -> R.drawable.calendar_day_background_default
                }
            )

            // Обработка клика
            itemView.setOnClickListener {
                if (!isSelected) { // Кликаем только по невыбранным дням
                    val previousSelectedDate = currentSelectedDate
                    currentSelectedDate = day.date

                    // Обновляем только предыдущий и текущий элементы
                    daysList.indexOfFirst { it.date == previousSelectedDate }.takeIf { it != -1 }?.let {
                        notifyItemChanged(it)
                    }
                    notifyItemChanged(adapterPosition)

                    onDaySelected(day)
                    onDateSelected(day.date)
                }
            }
        }
    }
}