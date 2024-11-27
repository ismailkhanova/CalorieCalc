package com.example.caloriecalc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Получаем переданное количество калорий из предыдущей активности (InputActivity)
        val calories = intent.getIntExtra("calories", 0)

        // Инициализируем элементы на экране
        val caloriesResultText = findViewById<TextView>(R.id.calories_result)
        val goalGroup = findViewById<RadioGroup>(R.id.goal_group)
        val startButton = findViewById<Button>(R.id.start_button)

        // Устанавливаем начальное количество калорий для поддержания веса
        caloriesResultText.text = "$calories ккал"

        // Обработчик нажатия на кнопку "Начать"
        startButton.setOnClickListener {
            // Переход на главный экран (InputActivity)
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
            finish()  // Закрыть текущую активность, чтобы не вернуться на нее
        }

        // Обработка изменения цели (Поддержание, Похудение или Набор веса)
        goalGroup.setOnCheckedChangeListener { _, checkedId ->
            // Рассчитываем новое количество калорий в зависимости от выбранной цели
            val newCalories = when (checkedId) {
                R.id.maintain_weight -> calories  // Поддержание веса (без изменений)
                R.id.lose_weight -> calories - 500  // Похудение (уменьшаем на 500 калорий)
                R.id.gain_weight -> calories + 500  // Набор веса (увеличиваем на 500 калорий)
                else -> calories  // На случай, если ничего не выбрано
            }

            // Обновляем текст с новым количеством калорий
            caloriesResultText.text = "$newCalories ккал"
        }
    }
}
