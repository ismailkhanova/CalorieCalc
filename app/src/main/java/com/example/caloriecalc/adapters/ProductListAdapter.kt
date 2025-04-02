package com.example.caloriecalc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.R
import com.example.caloriecalc.data.Product

class ProductListAdapter(
    private val products: List<Product> // Список продуктов
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_productlist, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position] // Получаем текущий продукт
        holder.bind(product) // Привязываем данные к ViewHolder
    }

    override fun getItemCount(): Int = products.size // Количество продуктов в списке

    // ViewHolder для одного продукта
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productNameTextView: TextView = itemView.findViewById(R.id.productName)
        private val productCaloriesTextView: TextView = itemView.findViewById(R.id.productCalories)
        private val productWeightTextView: TextView = itemView.findViewById(R.id.productWeight)
        private val productProteinTextView: TextView = itemView.findViewById(R.id.productProtein)
        private val productFatTextView: TextView = itemView.findViewById(R.id.productFat)
        private val productCarbsTextView: TextView = itemView.findViewById(R.id.productCarbs)


        fun bind(product: Product) {
            productNameTextView.text = product.name
            productCaloriesTextView.text = "${String.format("%.2f", product.calories)} ккал"
            productWeightTextView.text = "${String.format("%.2f", product.weight)} г"
            productProteinTextView.text = "Б: ${product.protein_g} г"
            productFatTextView.text = "Ж: ${product.fat_total_g} г"
            productCarbsTextView.text = "У: ${product.carbohydrates_total_g} г"
        }
    }
}