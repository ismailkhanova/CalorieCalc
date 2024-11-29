package com.example.caloriecalc

import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.caloriecalc.registration_user.LoginActivity
import com.example.caloriecalc.registration_user.RegisterActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим кнопку по ID
        val loginButton = findViewById<Button>(R.id.login_button)
        val signupButton = findViewById<Button>(R.id.signup_button)

        // Добавляем обработчик нажатия
        loginButton.setOnClickListener {
            // Переход на экран ввода данных
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signupButton.setOnClickListener {
            // Переход на экран ввода данных
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
