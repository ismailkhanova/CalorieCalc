package com.example.caloriecalc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecalc.ui.theme.CalorieCalcTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainScreenActivity : AppCompatActivity() {
        private lateinit var auth: FirebaseAuth
        private lateinit var database: DatabaseReference

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main_screen)

            // Инициализация FirebaseAuth и DatabaseReference
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance().reference

            // Находим TextView для отображения имени
            val welcomeText: TextView = findViewById(R.id.title)

            // Получаем UID текущего пользователя
            val currentUserUid = auth.currentUser?.uid
            if (currentUserUid != null) {
                // Получаем ссылку на данные пользователя в Firebase
                val userRef = database.child("users").child(currentUserUid)

                // Извлекаем имя пользователя
                userRef.child("userName").get().addOnSuccessListener { snapshot ->
                    val userName = snapshot.getValue(String::class.java)
                    if (!userName.isNullOrEmpty()) {
                        // Если имя пользователя не пустое, показываем его
                        welcomeText.text = "Профиль пользователя: $userName!"
                    } else {
                        // Если имя пустое, показываем "Гость"
                        welcomeText.text = "Добро пожаловать, Гость!"
                    }
                }.addOnFailureListener {
                    // В случае ошибки загрузки данных
                    welcomeText.text = "Ошибка загрузки имени"
                }
            } else {
                // Если текущий пользователь не авторизован
                welcomeText.text = "Пожалуйста, авторизуйтесь"
            }

            val logoutButton: Button = findViewById(R.id.logout_button)
            logoutButton.setOnClickListener {
                // Выйти из аккаунта
                auth.signOut()

                // Перенаправление на экран входа
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Закрываем текущую активность
            }
        }
    }
