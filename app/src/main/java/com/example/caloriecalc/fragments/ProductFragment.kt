package com.example.caloriecalc.fragments

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.caloriecalc.R
import com.example.caloriecalc.data.DiaryViewModel
import com.example.caloriecalc.data.Product
import com.example.caloriecalc.data.RecipeViewModel
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDate


class ProductFragment : Fragment() {
    private lateinit var viewModel: DiaryViewModel
    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private var isForRecipe: Boolean = false

    private val decimalFormat = DecimalFormat("#.##").apply {
        roundingMode = RoundingMode.DOWN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isForRecipe = arguments?.getBoolean("isForRecipe") ?: false
        Log.d("ProductFragment", "isForRecipe = $isForRecipe") // Для отладки
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(DiaryViewModel::class.java)
        val binding = inflater.inflate(R.layout.fragment_product, container, false)

        fun Double.roundToTwo(): Double {
            return decimalFormat.format(this).replace(",", ".").toDouble()
        }


        // Convert float arguments back to double for calculations
        val mealName = arguments?.getString("meal_name") ?: "Завтрак"
        val productName = arguments?.getString("product_name") ?: "Неизвестно"
        val productCalories = arguments?.getFloat("product_calories")?.toDouble() ?: 0.0
        val productProtein = arguments?.getFloat("protein")?.toDouble() ?: 0.0
        val productFat = arguments?.getFloat("fat")?.toDouble() ?: 0.0
        val productCarbs = arguments?.getFloat("carbs")?.toDouble() ?: 0.0

        val weightInput = binding.findViewById<EditText>(R.id.weightInput)
        val saveButton = binding.findViewById<Button>(R.id.saveButton)
        val nameTextView = binding.findViewById<TextView>(R.id.productNameTextView)
        val caloriesTextView = binding.findViewById<TextView>(R.id.productCaloriesTextView)
        val proteinTextView = binding.findViewById<TextView>(R.id.productProteinTextView)
        val fatTextView = binding.findViewById<TextView>(R.id.productFatTextView)
        val carbsTextView = binding.findViewById<TextView>(R.id.productCarbsTextView)

        nameTextView.text = productName
        caloriesTextView.text = "Калории: $productCalories ккал"
        proteinTextView.text = "Белки: $productProtein г"
        fatTextView.text = "Жиры: $productFat г"
        carbsTextView.text = "Углеводы: $productCarbs г"

        fun updateNutrition(weight: Double) {
            val factor = weight / 100
            caloriesTextView.text = "Калории: ${decimalFormat.format(productCalories * factor)} ккал"
            proteinTextView.text = "Белки: ${decimalFormat.format(productProtein * factor)} г"
            fatTextView.text = "Жиры: ${decimalFormat.format(productFat * factor)} г"
            carbsTextView.text = "Углеводы: ${decimalFormat.format(productCarbs * factor)} г"
        }

        updateNutrition(100.0)

        weightInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val weight = s?.toString()?.toDoubleOrNull() ?: 100.0
                updateNutrition(weight)
            }

            override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })



        saveButton.setOnClickListener {
            val weight = weightInput.text.toString().toDoubleOrNull() ?: 100.0

            val newProduct = Product(
                name = productName,
                weight = weight.roundToTwo(),
                calories = (productCalories * weight / 100).roundToTwo(),
                protein_g = (productProtein * weight / 100).roundToTwo(),
                fat_total_g = (productFat * weight / 100).roundToTwo(),
                carbohydrates_total_g = (productCarbs * weight / 100).roundToTwo()
            )

            if (isForRecipe) {
                // Получаем ViewModel из activity
                val recipeViewModel: RecipeViewModel by activityViewModels()
                recipeViewModel.addIngredient(newProduct)
                // Возвращаемся сразу в CreateRecipeFragment
                setFragmentResult("ingredient_added", bundleOf("ingredient" to newProduct))
                findNavController().navigateUp()
            } else {
                // Оригинальная логика для дневника
                val selectedDate = viewModel.selectedDate.value ?: LocalDate.now()
                viewModel.addProduct(selectedDate, mealName, newProduct)
                findNavController().navigateUp()
            }
        }

        return binding
    }
}