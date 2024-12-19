package com.example.caloriecalc.data

data class UserProfile(
    var height: Int? = null,
    var weight: Double? = null,
    var age: Int? = null,
    var gender: String? = null,
    var activityLevel: Double? = null,
    var calories: Int? = null,
    var goal: String? = null,
    var goalCalories: Int? = null
)
