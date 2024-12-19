package com.example.caloriecalc.data

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate,
    val isToday: Boolean = false,
    val isSelected: Boolean = false
)

