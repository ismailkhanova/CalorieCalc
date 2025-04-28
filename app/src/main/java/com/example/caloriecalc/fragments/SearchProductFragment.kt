package com.example.caloriecalc.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.caloriecalc.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.example.caloriecalc.adapters.ProductAdapter
import com.example.caloriecalc.api.CaloriesNinjasApi
import com.example.caloriecalc.data.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.Query
import kotlin.math.roundToInt


class SearchProductFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var cancelBtn: Button
    private val api = CaloriesNinjasApi.create()
    private var isForRecipe: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isForRecipe = arguments?.getBoolean("isForRecipe") ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_search_product, container, false)

        searchView = binding.findViewById(R.id.searchView)
        recyclerView = binding.findViewById(R.id.resultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        cancelBtn = binding.findViewById(R.id.btnCancel)

        cancelBtn.setOnClickListener {
            findNavController().navigateUp() // Navigate back to previous fragment
        }

        // Делаем так, чтобы при нажатии в любое место строки поиска она сразу активировалась
        searchView.setOnClickListener {
            searchView.isIconified = false
            searchView.requestFocus()
        }

        // Если поле поиска получает фокус, оно должно развернуться
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchView.isIconified = false
            }
        }

        // Принудительно фокусируем строку поиска, чтобы избежать двойного нажатия
        searchView.post {
            searchView.requestFocus()
        }

        adapter = ProductAdapter(emptyList()) { selectedProduct ->
            openProductFragment(selectedProduct)
        }
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchProducts(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        return binding
    }

    private fun openProductFragment(product: Product) {
        val mealName = arguments?.getString("meal_name") ?: "Завтрак"
        val args = Bundle().apply {
            putBoolean("isForRecipe", isForRecipe)
            putString("product_name", product.name)
            putFloat("product_calories", roundToTwoDecimals(product.calories).toFloat())
            putFloat("protein", roundToTwoDecimals(product.protein_g).toFloat())
            putFloat("fat", roundToTwoDecimals(product.fat_total_g).toFloat())
            putFloat("carbs", roundToTwoDecimals(product.carbohydrates_total_g).toFloat())

            if (!isForRecipe) {
                putString("meal_name", mealName)
            }

    }

        // Navigate to ProductFragment with NavController
        findNavController().navigate(
            R.id.action_searchProductFragment_to_productFragment,
            args
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return (value * 100).roundToInt() / 100.0
    }

    private fun searchProducts(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.searchProducts(query)
                withContext(Dispatchers.Main) {
                    adapter.updateData(response.items)
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Ошибка при загрузке данных", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}