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
import com.example.caloriecalc.adapters.ProductAdapter
import com.example.caloriecalc.api.CaloriesNinjasApi
import com.example.caloriecalc.data.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.Query


class SearchProductFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var cancelBtn: Button
    private val api = CaloriesNinjasApi.create()

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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiaryFragment())
                .addToBackStack(null) // Добавляет в стек, чтобы можно было вернуться назад
                .commit()
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
        val fragment = ProductFragment().apply {
            arguments = Bundle().apply {
                putString("meal_name", mealName)
                putString("product_name", product.name)
                putDouble("product_calories", product.calories)
                putDouble("protein", product.protein_g)
                putDouble("fat", product.fat_total_g)
                putDouble("carbs", product.carbohydrates_total_g)

            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun searchProducts(query: String){
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
