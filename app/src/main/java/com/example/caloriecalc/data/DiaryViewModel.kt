package com.example.caloriecalc.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class DiaryViewModel: ViewModel() {
    private val _foodData = MutableLiveData<MutableMap<LocalDate, MutableMap<String,
            MutableList<Product>>>>(mutableMapOf())
    val foodData: LiveData<MutableMap<LocalDate, MutableMap<String,
            MutableList<Product>>>> = _foodData
    private val _selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date

        if (_foodData.value?.containsKey(date) != true) {
            val currentData = _foodData.value ?: mutableMapOf()
            currentData[date] = mutableMapOf(
                "Завтрак" to mutableListOf(),
                "Обед" to mutableListOf(),
                "Ужин" to mutableListOf(),
                "Перекус" to mutableListOf()
            )
            _foodData.value = currentData
        }
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

        _foodData.value= currentData
    }
    fun getFoodForDate(date: LocalDate): Map<String, List<Product>>{
        return _foodData.value?.get(date) ?: emptyMap()
    }

}