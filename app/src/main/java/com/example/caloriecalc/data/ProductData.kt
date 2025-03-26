package com.example.caloriecalc.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ApiResponse(val items: List<Product>)

@Parcelize
data class Product(
    val name: String,
    val calories: Double,
    val protein_g: Double,
    val fat_total_g: Double,
    val weight: Double,
    val carbohydrates_total_g: Double
) : Parcelable
