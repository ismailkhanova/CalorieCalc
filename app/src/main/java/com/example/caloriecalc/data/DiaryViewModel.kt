package com.example.caloriecalc.data

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.translationMatrix
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.DayOfWeek
import java.time.LocalDate

class DiaryViewModel: ViewModel() {
    private val database = Firebase.database.reference
    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    private val cachedWeek = mutableMapOf<LocalDate, MutableMap<String, MutableList<Product>>>()
    private val _foodData = MutableLiveData<MutableMap<LocalDate, MutableMap<String,
            MutableList<Product>>>>(mutableMapOf())
    val foodData: LiveData<MutableMap<LocalDate, MutableMap<String,
            MutableList<Product>>>> = _foodData
    private val _selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    init {
        database.keepSynced(true)
        loadInitialWeekData()
    }

    fun loadInitialWeekData() {
        _isLoading.value = true
        val startDate = LocalDate.now().with(DayOfWeek.MONDAY)
        val endDate = startDate.plusDays(6)

        for (i in 0..6) {
            val currentDate = startDate.plusDays(i.toLong())
            loadDataForDate(currentDate)
        }
    }

    fun getMealsForDate(date: LocalDate): Map<String, List<Product>> {
        return cachedWeek[date]?.mapValues { it.value.toList() } ?: emptyMap()
    }



    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        loadDataForDate(date)
    }

    fun loadDataForDate(date: LocalDate) {
        val dateKey = date.toString()
        if (cachedWeek.containsKey(date)) {
            _foodData.value?.put(date, cachedWeek[date]!!)
            _foodData.value = _foodData.value
            return
        }

        database.child("users").child(currentUserId).child("meals").child(dateKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val mealsMap = mutableMapOf<String, MutableList<Product>>().apply {
                        put("Завтрак", mutableListOf())
                        put("Обед", mutableListOf())
                        put("Ужин", mutableListOf())
                        put("Перекус", mutableListOf())
                    }

                    snapshot.children.forEach { mealSnapshot ->
                        val mealName = mealSnapshot.key ?: return@forEach
                        val products = mealSnapshot.children.mapNotNull {
                            it.getValue(Product::class.java)
                        }.toMutableList()
                        mealsMap[mealName] = products
                    }
                    cachedWeek[date] = mealsMap

                    _foodData.value?.put(date, mealsMap)
                    _foodData.value = _foodData.value

                    val currentData = _foodData.value ?: mutableMapOf()
                    currentData[date] = mealsMap
                    _foodData.value = currentData

                    // Проверяем, загружена ли вся неделя
                    if (currentData.size == 7) {
                        _isLoading.value = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    Log.e("DiaryViewModel", "Error loading data for date", error.toException())
                }
            })
    }


    fun addProduct(date: LocalDate, mealName: String, product: Product) {
        val currentData = _foodData.value ?: mutableMapOf()

        val dateData = currentData.getOrPut(date){
            mutableMapOf(
                "Завтрак" to mutableListOf(),
                "Обед" to mutableListOf(),
                "Ужин" to mutableListOf(),
                "Перекус" to mutableListOf()
            )
        }

        dateData[mealName]?.add(product)

        // Обновляем только кеш и локальные данные
        cachedWeek[date] = currentData[date] ?: mutableMapOf()
        _foodData.value = currentData  // Обновляем LiveData

        // Сохраняем данные в Firebase
        saveProductToFirebase(date, mealName, product)
    }

    private fun saveProductToFirebase(date: LocalDate, mealName: String, product: Product) {
        val dateKey = date.toString()
        val productKey = database.child("users").child(currentUserId).child("meals")
            .child(dateKey).child(mealName).push().key ?: return

        database.child("users").child(currentUserId).child("meals")
            .child(dateKey).child(mealName).child(productKey)
            .setValue(product)
            .addOnFailureListener { e ->
                Log.e("DiaryViewModel", "Error saving product", e)
            }
    }

    fun getFoodForDate(date: LocalDate): Map<String, List<Product>>{
        return _foodData.value?.get(date) ?: emptyMap()
    }

}