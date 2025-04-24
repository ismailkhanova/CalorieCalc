package com.example.caloriecalc.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import com.google.firebase.database.database

class RecipeViewModel : ViewModel() {
    private val database = Firebase.database.reference
    private val _ingredients = MutableLiveData<MutableList<Ingredient>>(mutableListOf())
    val ingredients: LiveData<MutableList<Ingredient>> = _ingredients

    fun addIngredient(ingredient: Ingredient) {
        _ingredients.value = _ingredients.value?.apply { add(ingredient) }
    }

    fun removeIngredient(ingredient: Ingredient) {
        _ingredients.value = _ingredients.value?.apply { remove(ingredient) }
    }

    fun saveRecipe(
        name: String,
        cookwareWeight: Double,
        totalWeight: Double,
        onComplete: (Boolean) -> Unit
    ) {
        val ingredients = _ingredients.value ?: emptyList()
        val totalCalories = ingredients.sumOf { (it.caloriesPer100g * it.weight) / 100 }
        val netWeight = totalWeight - cookwareWeight
        val caloriesPer100g = if (netWeight > 0) (totalCalories * 100) / netWeight else 0.0

        val recipeId = database.child("recipes").push().key ?: ""
        val ingredientsMap = ingredients.associate { ingredient ->
            val ingredientId = database.push().key ?: ""
            ingredientId to mapOf(
                "name" to ingredient.name,
                "weight" to ingredient.weight,
                "calories_per_100g" to ingredient.caloriesPer100g
            )
        }

        database.child("recipes").child(recipeId).setValue(
            mapOf(
                "name" to name,
                "calories_per_100g" to caloriesPer100g,
                "ingredients" to ingredientsMap,
                "cookware_weight" to cookwareWeight,
                "total_weight" to totalWeight
            )
        ).addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}