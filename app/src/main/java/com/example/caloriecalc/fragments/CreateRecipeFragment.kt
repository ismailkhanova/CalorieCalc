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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.resetSaveStatus()



        setupRecyclerView()
        setupButtons()
        observeViewModel()
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
            val containerWeight = binding.etContainerWeight.text.toString().toDoubleOrNull() ?: 0.0
            val totalWeight = binding.etTotalWeight.text.toString().toDoubleOrNull() ?: 0.0

            if (containerWeight >= totalWeight) {
                Toast.makeText(requireContext(),
                    "Вес посуды не может быть больше общего веса",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.calculateCalories(containerWeight, totalWeight)
        }

        binding.btnSaveRecipe.setOnClickListener {
            val name = binding.etRecipeName.text.toString()
            val containerWeight = binding.etContainerWeight.text.toString().toDoubleOrNull() ?: 0.0
            val totalWeight = binding.etTotalWeight.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Введите название рецепта", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveRecipe(name, containerWeight, totalWeight)
            viewModel.currentRecipe.value?.let { recipe ->
                viewModel.saveRecipeToFirebase(recipe)
            }
        }
    }
    private fun Double.roundToTwo(): Double {
        return (this * 100).roundToInt() / 100.0
    }

    private fun observeViewModel() {
        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            Log.d("CreateRecipe", "Ingredients updated: ${ingredients.size} items")
            adapter.submitList(ingredients.toList())
        }


        viewModel.currentRecipe.observe(viewLifecycleOwner) { recipe ->
            binding.tvCaloriesPer100g.text =
                "Калорийность на 100 г: ${recipe.caloriesPer100g.roundToTwo()} ккал"
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { status ->
            status?.let { safeStatus -> // добавляем проверку на null
                when (safeStatus) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Рецепт сохранён", Toast.LENGTH_SHORT).show()
                        viewModel.clearRecipeData()
                        viewModel.resetSaveStatus() // Сбрасываем статус
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), safeStatus.message, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        // Можно показать ProgressBar
                    }
                }
            }
        }

    }
}
