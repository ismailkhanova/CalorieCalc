package com.example.caloriecalc.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.FirebaseDatabase

class RecipeViewModel : ViewModel() {
    private val _ingredients = MutableLiveData<List<Product>>(emptyList())
    val ingredients: LiveData<List<Product>> = _ingredients

    private val _currentRecipe = MutableLiveData<Recipe>(Recipe())
    val currentRecipe: LiveData<Recipe> = _currentRecipe

    private val _saveStatus = MutableLiveData<Resource<Unit>?>(null)
    val saveStatus: LiveData<Resource<Unit>?> = _saveStatus



    fun addIngredient(ingredient: Product) {
        val currentList = _ingredients.value ?: emptyList()
        Log.d("RecipeVM", "Adding ingredient: ${ingredient.name}, current count: ${currentList.size}")
        _ingredients.value = currentList + ingredient
    }

    fun removeIngredient(ingredient: Product) {
        val currentList = _ingredients.value ?: emptyList()
        _ingredients.value = currentList.filter { it != ingredient }
    }

    fun calculateCalories(containerWeight: Double, totalWeight: Double) {
        val ingredients = _ingredients.value ?: return
        val totalCalories = ingredients.sumOf { it.calories }
        val foodWeight = totalWeight - containerWeight

        val caloriesPer100g = if (foodWeight > 0) {
            (totalCalories * 100) / foodWeight
        } else 0.0

        _currentRecipe.value = _currentRecipe.value!!.copy(
            caloriesPer100g = caloriesPer100g
        )
    }

    fun saveRecipe(name: String, containerWeight: Double, totalWeight: Double) {
        val ingredients = _ingredients.value ?: emptyList()
        val totalCalories = ingredients.sumOf { it.calories }
        val foodWeight = totalWeight - containerWeight

        val caloriesPer100g = if (foodWeight > 0) {
            (totalCalories * 100) / foodWeight
        } else 0.0

        _currentRecipe.value = Recipe(
            name = name,
            ingredients = ingredients,
            caloriesPer100g = caloriesPer100g
        )
    }

    fun saveRecipeToFirebase(recipe: Recipe) {
        _saveStatus.value = Resource.Loading()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            _saveStatus.value = Resource.Error("Пользователь не авторизован")
            return
        }

        val database = FirebaseDatabase.getInstance()
        val recipesRef = database.getReference("users/$userId/recipes")

        val recipeId = recipesRef.push().key ?: run {
            _saveStatus.value = Resource.Error("Ошибка создания ID рецепта")
            return
        }

        // Конвертируем список ингредиентов в Map для Firebase
        val ingredientsMap = recipe.ingredients.mapIndexed { index, ingredient ->
            "ingredient$index" to mapOf(
                "name" to ingredient.name,
                "calories" to ingredient.calories,
                "protein_g" to ingredient.protein_g,
                "fat_total_g" to ingredient.fat_total_g,
                "carbohydrates_total_g" to ingredient.carbohydrates_total_g,
                "weight" to ingredient.weight
            )
        }.toMap()

        val recipeData = mapOf(
            "name" to recipe.name,
            "ingredients" to ingredientsMap,
            "caloriesPer100g" to recipe.caloriesPer100g
        )

        recipesRef.child(recipeId).setValue(recipeData)
            .addOnSuccessListener {
                _saveStatus.value = Resource.Success(Unit)
            }
            .addOnFailureListener { e ->
                _saveStatus.value = Resource.Error(e.message ?: "Ошибка сохранения")
            }
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
    }

    fun clearRecipeData() {
        _ingredients.value = emptyList()
        _currentRecipe.value = Recipe()
        _saveStatus.value = null
    }


//    fun loadRecipes() {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//
//        val database = FirebaseDatabase.getInstance()
//        val recipesRef = database.getReference("users/$userId/recipes")
//
//        recipesRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val recipes = snapshot.children.mapNotNull { recipeSnapshot ->
//                    recipeSnapshot.getValue(Recipe::class.java)?.copy(id = recipeSnapshot.key ?: "")
//                }
//                _recipesList.value = recipes
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("Firebase", "Error loading recipes", error.toException())
//            }
//        })
//    }
}

// Класс-обёртка для статуса операции
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}