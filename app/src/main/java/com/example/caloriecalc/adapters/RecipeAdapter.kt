package com.example.caloriecalc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.R
import com.example.caloriecalc.data.Recipe
import com.example.caloriecalc.databinding.ItemRecipeBinding


class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit,

): ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
       val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(
        private val binding: ItemRecipeBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(recipe: Recipe) = with(binding) {
            recipeName.text = recipe.name
            recipeCalories.text = "Ккал 100г: ${recipe.caloriesPer100g.roundToTwo()} ккал"
            productProtein.text =  "Б: ${recipe.protein.roundToTwo()}г"
            productFat.text = "Ж: ${recipe.fat.roundToTwo()}г"
            productCarbs.text = "У: ${recipe.carbs.roundToTwo()}г"

            root.setOnClickListener {
               onRecipeClick(recipe)
            }
        }

        private fun Double.roundToTwo(): String {
            return String.format("%.2f", this)
        }
    }

    class DiffCallBack : DiffUtil.ItemCallback<Recipe>(){
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }



}