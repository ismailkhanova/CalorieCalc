package com.example.caloriecalc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.R
import com.example.caloriecalc.data.Product

class ProductAdapter(private var products: List<Product>,
    private val onItemClick: (Product) -> Unit): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // вложенный класс, содержит ссылки на элементы разметки
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.productName)
        val calories: TextView = itemView.findViewById(R.id.productCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    //заполнение данных в каждом элементе списка
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.name.text = product.name
        holder.calories.text = "Калорий в 100г: ${product.calories} ккал"

        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    // кол-во элементов в списке
    override fun getItemCount() = products.size

    //обновление списка данных
    fun updateData(newProducts: List<Product>){
        products = newProducts;
        notifyDataSetChanged()
    }
}