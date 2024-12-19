package com.example.caloriecalc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Получаем переданное количество калорий из предыдущей активности (InputActivity)
        val initialCalories = intent.getIntExtra("calories", 0)

        // Инициализируем элементы на экране
        val caloriesResultText = findViewById<TextView>(R.id.calories_result)
        val goalGroup = findViewById<RadioGroup>(R.id.goal_group)
        val startButton = findViewById<Button>(R.id.start_button)

        // Устанавливаем начальное количество калорий для поддержания веса
        var selectedCalories = initialCalories
        caloriesResultText.text = "$selectedCalories ккал"

        // Обработка изменения цели (Поддержание, Похудение или Набор веса)
        goalGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedCalories = when (checkedId) {
                R.id.maintain_weight -> initialCalories  // Поддержание веса
                R.id.lose_weight -> initialCalories - 500  // Похудение
                R.id.gain_weight -> initialCalories + 500  // Набор веса
                else -> initialCalories  // Если ничего не выбрано
            }

            // Обновляем текст с новым количеством калорий
            caloriesResultText.text = "$selectedCalories ккал"
        }

        // Обработчик нажатия на кнопку "Начать"
        startButton.setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                val profileRef = FirebaseDatabase.getInstance().reference.child("profiles").child(currentUserId)

                // Определяем цель
                val selectedGoal = when (goalGroup.checkedRadioButtonId) {
                    R.id.maintain_weight -> "Поддержание"
                    R.id.lose_weight -> "Похудение"
                    R.id.gain_weight -> "Набор"
                    else -> "Поддержание"
                }

                // Сохраняем цель и калорийность в Firebase
                val profileUpdates = mapOf(
                    "goal" to selectedGoal,
                    "goalCalories" to selectedCalories
                )

                profileRef.updateChildren(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Переход на главный экран
                        val intent = Intent(this, MainScreenActivity::class.java)
                        startActivity(intent)
                        finish() // Закрыть текущую активность
                    } else {
                        Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
