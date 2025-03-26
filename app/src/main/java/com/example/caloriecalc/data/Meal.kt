package com.example.caloriecalc.data

data class Meal(
    val name: String, // Название приема пищи (Завтрак, Обед и т. д.)
    val products: MutableList<Product> = mutableListOf() // Список добавленных продуктов
) {
    // Функции для подсчёта общей калорийности и БЖУ
    fun getTotalCalories(): Double {
        return products.sumOf { it.calories }
    }

    fun getTotalProtein(): Double {
        return products.sumOf { it.protein_g }
    }

    fun getTotalFat(): Double {
        return products.sumOf { it.fat_total_g }
    }

    fun getTotalCarbs(): Double {
        return products.sumOf { it.carbohydrates_total_g }
    }
}
