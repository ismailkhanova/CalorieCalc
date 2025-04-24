package com.example.caloriecalc.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val id: String = "",
    val name: String = "",
    val caloriesPer100g: Double = 0.0,
    val ingredients: Map<String, Ingredient> = emptyMap(),
    val cookwareWeight: Double = 0.0,
    val totalWeight: Double = 0.0
) : Parcelable

@Parcelize
data class Ingredient(
    val id: String = "",
    val name: String = "",
    val weight: Double = 0.0,
    val caloriesPer100g: Double = 0.0
) : Parcelable
