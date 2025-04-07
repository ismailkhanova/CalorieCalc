package com.example.caloriecalc.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ApiResponse(val items: List<Product>)

@Parcelize
data class Product(
    val name: String = "",               // Добавляем значения по умолчанию
    val calories: Double = 0.0,
    val protein_g: Double = 0.0,
    val fat_total_g: Double = 0.0,
    val weight: Double = 0.0,
    val carbohydrates_total_g: Double = 0.0
) : Parcelable {
    // Добавляем пустой конструктор для Firebase
    constructor() : this("", 0.0, 0.0, 0.0, 0.0, 0.0)
}
