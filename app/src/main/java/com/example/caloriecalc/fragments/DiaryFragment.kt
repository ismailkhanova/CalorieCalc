package com.example.caloriecalc.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.DaysAdapter
import com.example.caloriecalc.R
import com.example.caloriecalc.data.CalendarDay
import java.time.DayOfWeek
import java.time.LocalDate

class DiaryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var daysAdapter: DaysAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewDays)

        setupRecyclerView()
        loadCurrentWeek()  // Загружаем текущую неделю

        return view
    }

    private fun setupRecyclerView() {
        daysAdapter = DaysAdapter { selectedDay ->
            loadFoodDataForDate(selectedDay.date)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = daysAdapter
        }
    }

    // Метод для загрузки текущей недели
    private fun loadCurrentWeek() {
        val today = LocalDate.now()
        val currentWeek = mutableListOf<LocalDate>()

        for (i in 0..6) {
            val currentDate = today.with(DayOfWeek.MONDAY).plusDays(i.toLong())
            currentWeek.add(currentDate)
        }

        // Обновляем список дней, выделяя текущий день
        updateDays(currentWeek)
    }

    // Метод для обновления дней
    private fun updateDays(days: List<LocalDate>) {
        val today = LocalDate.now()  // Текущая дата
        val updatedDays = days.map { date ->
            val isToday = date.isEqual(today)
            CalendarDay(date, isToday)
        }
        daysAdapter.submitList(updatedDays)
    }

    private fun loadFoodDataForDate(date: LocalDate) {
        // Логика для загрузки данных для выбранной даты
    }
}
