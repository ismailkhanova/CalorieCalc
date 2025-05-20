package com.example.caloriecalc.data

data class NutrientSummary(
    val calories: Double,
    val protein: Double,
    val fats: Double,
    val carbs: Double
)

data class NutrientUIModel(
    val caloriesText: String,
    val proteinText: String,
    val fatsText: String,
    val carbsText: String,
    val caloriesInPercentText: String,
    val pieChartData: Triple<Float, Float, Float>,
    val kcalChartData: Pair<Float, Float>,
    val usedKcalText: String,
    val leftKcalText: String
)

