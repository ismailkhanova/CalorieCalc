package com.example.caloriecalc.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.DayOfWeek
import java.time.LocalDate

class StatisticViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    private val _weeklyCalories = MutableLiveData<List<Int>>(listOf(0, 0, 0, 0, 0, 0, 0))
    val weeklyCalories: LiveData<List<Int>> = _weeklyCalories

    private val _goalCalories = MutableLiveData<Int>()
    val goalCalories: LiveData<Int> = _goalCalories

    private val _weeklyDates = MutableLiveData<List<String>>(listOf())
    val weeklyDates: LiveData<List<String>> = _weeklyDates

    private val _weeklyNutrients = MutableLiveData<List<Triple<Double, Double, Double>>>(List(7) { Triple(0.0, 0.0, 0.0) })
    val weeklyNutrients: LiveData<List<Triple<Double, Double, Double>>> = _weeklyNutrients

    init {
        loadWeeklyCalories()
        loadUserGoalCalories()
        loadWeeklyNutrients()
    }

    private fun loadWeeklyCalories() {
        val startDate = LocalDate.now().with(DayOfWeek.MONDAY)
        val weeklyData = MutableList(7) { 0 }
        val weeklyDatesList = MutableList(7) { "" }

        for (i in 0..6) {
            val currentDate = startDate.plusDays(i.toLong())
            val dateKey = currentDate.toString()
            weeklyDatesList[i] = "${currentDate.dayOfWeek.name.take(3)}, ${currentDate.dayOfMonth}"

            database.child("users").child(currentUserId).child("meals").child(dateKey)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var dailyCalories = 0
                        snapshot.children.forEach { mealSnapshot ->
                            mealSnapshot.children.forEach { productSnapshot ->
                                val product = productSnapshot.getValue(Product::class.java)
                                dailyCalories += product?.calories?.toInt() ?: 0
                            }
                        }
                        weeklyData[i] = dailyCalories
                        _weeklyCalories.value = weeklyData
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
        _weeklyDates.value = weeklyDatesList
    }

    private fun loadUserGoalCalories() {
        database.child("profiles").child(currentUserId).child("goalCalories")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val goal = snapshot.getValue(Int::class.java)
                    _goalCalories.value = goal ?: 2000
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun loadWeeklyNutrients() {
        val startDate = LocalDate.now().with(DayOfWeek.MONDAY)
        val weeklyData = MutableList(7) { Triple(0.0, 0.0, 0.0) }

        for (i in 0..6) {
            val currentDate = startDate.plusDays(i.toLong())
            val dateKey = currentDate.toString()

            database.child("users").child(currentUserId).child("meals").child(dateKey)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var totalProtein = 0.0
                        var totalFats = 0.0
                        var totalCarbs = 0.0

                        snapshot.children.forEach { mealSnapshot ->
                            mealSnapshot.children.forEach { productSnapshot ->
                                val product = productSnapshot.getValue(Product::class.java)
                                totalProtein += product?.protein_g ?: 0.0
                                totalFats += product?.fat_total_g ?: 0.0
                                totalCarbs += product?.carbohydrates_total_g ?: 0.0
                            }
                        }
                        weeklyData[i] = Triple(totalProtein, totalFats, totalCarbs)
                        _weeklyNutrients.value = weeklyData
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }
}