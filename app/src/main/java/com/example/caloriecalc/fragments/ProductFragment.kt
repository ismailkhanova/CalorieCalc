package com.example.caloriecalc.fragments

import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import com.example.caloriecalc.R
import com.example.caloriecalc.data.Product


class ProductFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_product, container, false)

        val productName = arguments?.getString("product_name") ?: "Неизвестно"
        val productCalories = arguments?.getDouble("product_calories") ?: 0.0
        val productProtein = arguments?.getDouble("protein") ?: 0.0
        val productFat = arguments?.getDouble("fat") ?: 0.0
        val productCarbs = arguments?.getDouble("carbs") ?: 0.0


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
            caloriesTextView.text = "Калории: ${String.format("%.2f", productCalories * factor)} ккал"
            proteinTextView.text = "Белки: ${String.format("%.2f", productProtein * factor)} г"
            fatTextView.text = "Жиры: ${String.format("%.2f", productFat * factor)} г"
            carbsTextView.text = "Углеводы: ${String.format("%.2f", productCarbs * factor)} г"
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
                weight = weight,
                calories = productCalories * weight / 100,
                protein_g = productProtein * weight / 100,
                fat_total_g = productFat * weight / 100,
                carbohydrates_total_g = productCarbs * weight / 100
            )
            val mealName = arguments?.getString("meal_name")
            parentFragmentManager.setFragmentResult(
                "product_added",
                bundleOf(
                    "meal_name" to mealName,
                    "new_product" to newProduct as Parcelable)
            )

            parentFragmentManager.popBackStack()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchProductFragment()) // Заменяет текущий фрагмент на SearchProductFragment
                .addToBackStack(null) // Добавляет в стек, чтобы можно было вернуться назад
                .commit()
        }

        return binding
    }

}