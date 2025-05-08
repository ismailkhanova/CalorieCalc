package com.example.caloriecalc.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RecipeViewModel : ViewModel() {
    private val _ingredients = MutableLiveData<List<Product>>(emptyList())
    val ingredients: LiveData<List<Product>> = _ingredients

    private val _currentRecipe = MutableLiveData<Recipe>(Recipe())
    val currentRecipe: LiveData<Recipe> = _currentRecipe

    private val _saveStatus = MutableLiveData<Resource<Unit>?>(null)
    val saveStatus: LiveData<Resource<Unit>?> = _saveStatus

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    fun setIngredients(newIngredients: List<Product>) {
        _ingredients.value = newIngredients.toMutableList() // Используем mutable список
    }



    fun addIngredient(ingredient: Product) {
        val currentList = _ingredients.value?.toMutableList() ?: mutableListOf()
        if (!currentList.any { it.name == ingredient.name }) { // Проверка на дубликаты
            currentList.add(ingredient)
            _ingredients.value = currentList
            Log.d("RecipeVM", "Added ingredient: ${ingredient.name}")
        } else {
            Log.d("RecipeVM", "Ingredient ${ingredient.name} already exists")
        }
    }

    fun removeIngredient(ingredient: Product) {
        val currentList = _ingredients.value ?: emptyList()
        _ingredients.value = currentList.filter { it != ingredient }
    }

    fun calculateCalories(containerWeight: Double, totalWeight: Double) {
        val ingredients = _ingredients.value ?: return
        val totalCalories = ingredients.sumOf { it.calories }
        val protein = ingredients.sumOf { it.protein_g }
        val fat = ingredients.sumOf { it.fat_total_g }
        val carbs = ingredients.sumOf { it.carbohydrates_total_g }
        val foodWeight = totalWeight - containerWeight

        val factor = if (foodWeight > 0) 100 / foodWeight else 0.0

        _currentRecipe.value = _currentRecipe.value!!.copy(
            caloriesPer100g = totalCalories * factor,
            protein = protein * factor,
            fat = fat * factor,
            carbs = carbs * factor
        )
    }

    fun saveRecipe(name: String, containerWeight: Double, totalWeight: Double) {
        val ingredients = _ingredients.value ?: emptyList()
        val totalCalories = ingredients.sumOf { it.calories }
        val foodWeight = totalWeight - containerWeight
        val totalProtein = ingredients.sumOf { it.protein_g }
        val totalFat = ingredients.sumOf { it.fat_total_g }
        val totalCarbs = ingredients.sumOf { it.carbohydrates_total_g }

        val factor = if (foodWeight > 0) 100 / foodWeight else 0.0


        _currentRecipe.value = Recipe(
            name = name,
            ingredients = ingredients,
            caloriesPer100g = totalCalories*factor,
            containerWeight = containerWeight,
            totalWeight = totalWeight,
            protein = totalProtein * factor,
            fat = totalFat * factor,
            carbs = totalCarbs * factor

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

        // Здесь просто передаём список ингредиентов как есть — Firebase сам его сериализует в массив
        val recipeData = mapOf(
            "name" to recipe.name,
            "ingredients" to recipe.ingredients, // это List<Ingredient>
            "caloriesPer100g" to recipe.caloriesPer100g,
            "containerWeight" to recipe.containerWeight,
            "totalWeight" to recipe.totalWeight,
            "protein" to recipe.protein,
            "fat" to recipe.fat,
            "carbs" to recipe.carbs
        )

        recipesRef.child(recipeId).setValue(recipeData)
            .addOnSuccessListener {
                _saveStatus.value = Resource.Success(Unit)
            }
            .addOnFailureListener { e ->
                _saveStatus.value = Resource.Error(e.message ?: "Ошибка сохранения")
            }
    }

    fun updateRecipeInFirebase(recipe: Recipe) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("users/$userId/recipes/${recipe.id}")
            .setValue(recipe)
            .addOnSuccessListener {
                _saveStatus.value = Resource.Success(Unit)
            }
            .addOnFailureListener { e ->
                _saveStatus.value = Resource.Error(e.message ?: "Ошибка обновления")
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

    fun loadRecipes(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val recipesRef = database.getReference("users/$userId/recipes")

        recipesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipes = snapshot.children.mapNotNull { recipeSnapshot ->
                    recipeSnapshot.getValue(Recipe::class.java)?.copy(id = recipeSnapshot.key ?: "")
                }
                _recipes.value = recipes
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading recipes", error.toException())
            }
        })
    }
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