package com.example.caloriecalc.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.R
import com.example.caloriecalc.adapters.IngredientsAdapter
import com.example.caloriecalc.data.Product
import com.example.caloriecalc.data.Recipe
import com.example.caloriecalc.data.RecipeViewModel
import com.example.caloriecalc.data.Resource
import com.example.caloriecalc.databinding.FragmentCreateRecipeBinding
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.roundToInt

class CreateRecipeFragment : Fragment() {
    private lateinit var binding: FragmentCreateRecipeBinding
    private val viewModel: RecipeViewModel by activityViewModels()
    private lateinit var adapter: IngredientsAdapter
    private var isEditMode = false
    private var currentRecipeId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener("ingredient_added", viewLifecycleOwner) { _, bundle ->
            val ingredient = bundle.getParcelable<Product>("ingredient")
            ingredient?.let {
                viewModel.addIngredient(it)
            }
        }

        setupRecyclerView()
        checkEditMode()
        setupButtons()
        observeViewModel()
    }

    private fun checkEditMode() {
        arguments?.let { bundle ->
            val recipeArgs = CreateRecipeFragmentArgs.fromBundle(bundle).recipe
            if (recipeArgs != null) {
                // Если recipeArgs не null, то активируем режим редактирования
                isEditMode = true
                currentRecipeId = recipeArgs.id
                populateFields(recipeArgs)
            } else {
                // Если recipeArgs == null, это означает, что мы создаём новый рецепт
                isEditMode = false
                currentRecipeId = null
            }
        }
    }


    private fun populateFields(recipe: Recipe) {
        with(binding) {
            etRecipeName.setText(recipe.name)
            etContainerWeight.setText(recipe.containerWeight.toString())
            etTotalWeight.setText(recipe.totalWeight.toString())
            btnSaveRecipe.text = "Обновить рецепт"
        }
        viewModel.setIngredients(recipe.ingredients)

    }

    private fun setupRecyclerView() {
        adapter = IngredientsAdapter(
            onDeleteClick = { ingredient ->
                viewModel.removeIngredient(ingredient)
            }
        )

        binding.rvIngredients.adapter = adapter
        binding.rvIngredients.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupButtons() {
        binding.btnAddIngredient.setOnClickListener {
            findNavController().navigate(
                R.id.action_createRecipeFragment_to_searchProductFragment,
                Bundle().apply { putBoolean("isForRecipe", true) }
            )
        }

        binding.btnCalculate.setOnClickListener {
            calculateCalories()
        }

        binding.btnSaveRecipe.setOnClickListener {
            if (validateFields()) {
                if (isEditMode) {
                    updateRecipe()
                } else {
                    createRecipe()
                }
            }
        }
    }

    private fun calculateCalories() {
        val containerWeight = binding.etContainerWeight.text.toString().toDoubleOrNull() ?: 0.0
        val totalWeight = binding.etTotalWeight.text.toString().toDoubleOrNull() ?: 0.0

        if (containerWeight >= totalWeight) {
            Toast.makeText(
                requireContext(),
                "Вес посуды не может быть больше общего веса",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        viewModel.calculateCalories(containerWeight, totalWeight)
    }

    private fun validateFields(): Boolean {
        return when {
            binding.etRecipeName.text?.isBlank() == true -> {
                Toast.makeText(requireContext(), "Введите название рецепта", Toast.LENGTH_SHORT).show()
                false
            }

            viewModel.ingredients.value.isNullOrEmpty() -> {
                Toast.makeText(requireContext(), "Добавьте хотя бы один ингредиент", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun createRecipe() {
        val name = binding.etRecipeName.text.toString()
        val containerWeight = binding.etContainerWeight.text.toString().toDoubleOrNull() ?: 0.0
        val totalWeight = binding.etTotalWeight.text.toString().toDoubleOrNull() ?: 0.0

        viewModel.saveRecipe(name, containerWeight, totalWeight)
        viewModel.currentRecipe.value?.let { recipe ->
            viewModel.saveRecipeToFirebase(recipe)
        }
    }

    private fun updateRecipe() {
        val name = binding.etRecipeName.text.toString()
        val containerWeight = binding.etContainerWeight.text.toString().toDoubleOrNull() ?: 0.0
        val totalWeight = binding.etTotalWeight.text.toString().toDoubleOrNull() ?: 0.0

        viewModel.saveRecipe(name, containerWeight, totalWeight)
        viewModel.currentRecipe.value?.let { recipe ->
            val updatedRecipe = recipe.copy(
                id = currentRecipeId ?: return@let,
                name = name,
                ingredients = viewModel.ingredients.value ?: emptyList()
            )
            viewModel.updateRecipeInFirebase(updatedRecipe)
        }
    }

    private fun observeViewModel() {
        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            adapter.submitList(ingredients.toList())
        }

        viewModel.currentRecipe.observe(viewLifecycleOwner) { recipe ->
            recipe?.let {
                binding.tvCaloriesPer100g.text = "Калорийность на 100 г: ${recipe.caloriesPer100g.roundToTwo()} ккал"
                binding.protein.text = "Белки: ${recipe.protein.roundToTwo()} г"
                binding.fat.text = "Жиры: ${recipe.fat.roundToTwo()} г"
                binding.carbs.text = "Углеводы: ${recipe.carbs.roundToTwo()} г"
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(),
                        if (isEditMode) "Рецепт обновлён" else "Рецепт сохранён",
                        Toast.LENGTH_SHORT).show()
                    viewModel.clearRecipeData()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), status.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Показать индикатор загрузки
                }
                else -> {}
            }
        }
    }

    private fun Double.roundToTwo(): Double = (this * 100).roundToInt() / 100.0
}