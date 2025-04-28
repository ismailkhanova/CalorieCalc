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
import com.example.caloriecalc.data.Product
import com.example.caloriecalc.databinding.ItemIngredientBinding
import java.math.RoundingMode
import java.text.DecimalFormat


class IngredientsAdapter(
    private val onDeleteClick: (Product) -> Unit,

) : ListAdapter<Product, IngredientsAdapter.IngredientViewHolder>(DiffCallback()) {

    class IngredientViewHolder(private val binding: ItemIngredientBinding) :
        RecyclerView.ViewHolder(binding.root) {


        private val decimalFormat = DecimalFormat("#.##").apply {
            roundingMode = RoundingMode.DOWN
        }
        fun Double.roundToTwo(): Double {
            return decimalFormat.format(this).replace(",", ".").toDouble()
        }

        fun bind(ingredient: Product, onDeleteClick: (Product) -> Unit) {
            binding.apply {
                IngredientName.text = ingredient.name
                IngredientWeight.text = "${ingredient.weight.roundToTwo()} г"
                IngredientCalories.text = "${ingredient.calories.roundToTwo()} ккал"

                btnRemove.setOnClickListener {
                    onDeleteClick(ingredient)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) =
            oldItem.name == newItem.name && oldItem.weight == newItem.weight

        override fun areContentsTheSame(oldItem: Product, newItem: Product) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIngredientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteClick)
    }
}