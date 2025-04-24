package com.example.caloriecalc.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.caloriecalc.R


class RecipeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addRecipe: Button = view.findViewById(R.id.add_recipe_btn)

        addRecipe.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateRecipeFragment()) // Меняем текущий фрагмент
                .addToBackStack(null) // Добавляем в backstack для возможности вернуться назад
                .commit()
        }
    }
}