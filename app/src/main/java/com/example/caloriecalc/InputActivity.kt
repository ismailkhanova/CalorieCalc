package com.example.caloriecalc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.caloriecalc.data.UserProfile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import kotlin.math.roundToInt

class InputActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val welcomeText: TextView = findViewById(R.id.welcome_text)


        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null){
            val userRef = database.child("users").child(currentUserUid)
            userRef.child("userName").get().addOnSuccessListener { snapshot ->
                val userName = snapshot.getValue(String::class.java)
                if (!userName.isNullOrEmpty()) {
                    welcomeText.text = "Добро пожаловать, $userName!"
                } else {
                    welcomeText.text = "Добро пожаловать, Гость!"
                }
            }.addOnFailureListener {
                Log.e("FirebaseError", "Ошибка загрузки имени: ${it.message}")
                welcomeText.text = "Ошибка загрузки имени"
                Log.d("FirebasePath", "Путь: ${userRef.child("userName").toString()}")
            }
        }




        // Настройка Spinner для выбора уровня активности
        val activitySpinner = findViewById<Spinner>(R.id.activity_spinner)
        val activityLevels = arrayOf(
            "Нет активности",
            "Легкая активность",
            "Умеренная активность",
            "Интенсивная активность",
            "Очень высокая активность"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activitySpinner.adapter = adapter

        val calculateButton = findViewById<Button>(R.id.calculate_button)

        calculateButton.setOnClickListener {
            // Получение данных с экрана
            val height = findViewById<EditText>(R.id.height_input).text.toString().toInt()
            val weight = findViewById<EditText>(R.id.weight_input).text.toString().toDouble()
            val age = findViewById<EditText>(R.id.age_input).text.toString().toInt()
            val gender = if (findViewById<RadioButton>(R.id.male_button).isChecked) "Мужчина" else "Женщина"
            val activityLevel = getActivityLevel(findViewById<Spinner>(R.id.activity_spinner).selectedItem.toString())

            // Расчет калорий
            val calories = calculateCalories(height, weight, age, gender, activityLevel)

            val userProfile = UserProfile(
                height = height,
                weight = weight,
                age = age,
                gender = gender,
                activityLevel = activityLevel,
                calories = calories.roundToInt()
            )

            val currentUserid = auth.currentUser?.uid
            if(currentUserid != null) {
                val profileRef = database.child("profiles").child(currentUserid)
                profileRef.setValue(userProfile).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        // Переход на экран с результатами
                        val intent = Intent(this, ResultActivity::class.java)
                        intent.putExtra("calories", calories.roundToInt())
                        startActivity(intent)

                    } else {
                        Toast.makeText(this, "Ошибка сохранения данных профиля", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Метод для расчета калорий
    private fun calculateCalories(height: Int, weight: Double, age: Int, gender: String, activityLevel: Double): Double {
        val bmr = if (gender == "Мужчина") {
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            10 * weight + 6.25 * height - 5 * age - 161
        }
        return bmr * activityLevel
    }

    // Метод для получения коэффициента активности
    private fun getActivityLevel(activity: String): Double {
        return when (activity) {
            "Нет активности" -> 1.2
            "Легкая активность" -> 1.375
            "Умеренная активность" -> 1.55
            "Интенсивная активность" -> 1.725
            "Очень высокая активность" -> 1.9
            else -> 1.2
        }
    }
}
