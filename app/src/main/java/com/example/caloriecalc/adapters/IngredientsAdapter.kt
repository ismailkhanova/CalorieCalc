package com.example.caloriecalc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.R
import com.example.caloriecalc.data.Ingredient


class IngredientsAdapter(
    private var ingredients: List<Ingredient>,
    private val onRemoveClick: (Ingredient) -> Unit
) : RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.IngredientName)
        private val weightTextView: TextView = itemView.findViewById(R.id.IngredientWeight)
        private val removeButton: Button = itemView.findViewById(R.id.btnRemove)

        fun bind(ingredient: Ingredient) {
            nameTextView.text = ingredient.name
            weightTextView.text = "${ingredient.weight.toInt()}г (${"%.1f".format(ingredient.caloriesPer100g)} ккал/100г)"
            removeButton.setOnClickListener { onRemoveClick(ingredient) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ingredient, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size

    fun updateData(newIngredients: List<Ingredient>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }
}
