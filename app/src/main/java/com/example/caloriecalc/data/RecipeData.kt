package com.example.caloriecalc.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Recipe(
    val id: String = "",
    val name: String = "",
    val ingredients: List<Product> = emptyList(),
    val caloriesPer100g: Double = 0.0
) : Parcelable {
    constructor() : this("", "", emptyList(), 0.0)
}