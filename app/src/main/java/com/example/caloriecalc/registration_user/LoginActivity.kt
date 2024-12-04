package com.example.caloriecalc.registration_user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.caloriecalc.InputActivity
import com.example.caloriecalc.MainScreenActivity
import com.example.caloriecalc.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingLayout: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailInput:EditText = findViewById(R.id.email_input)
        val passwordInput:EditText = findViewById(R.id.password_input)
        val loginBtn:Button = findViewById(R.id.login_button)
        val registerLink: TextView = findViewById(R.id.register_link)
        val fPassword: TextView = findViewById(R.id.forgot_pwd)
        progressBar = findViewById(R.id.progress_bar)
        loadingLayout = findViewById(R.id.loading_layout)


        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java )
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if(email.isEmpty()|| password.isEmpty()){
                if(email.isEmpty()){
                    emailInput.error = "Введите свой email"
                }
                if (password.isEmpty()){
                    passwordInput.error = "Введите свой пароль"
                }
                Toast.makeText(this, "Введите свои данные", Toast.LENGTH_LONG).show()
            }

            else if(!email.matches(emailPattern.toRegex())){
                emailInput.error = "Введите корректный email-адрес"
                Toast.makeText(this, "Некорретный email-адрес", Toast.LENGTH_LONG).show()
            }
            else if(password.length<8){
                passwordInput.error="Пароль должен быть больше 8 символов"
                Toast.makeText(this, "Пароль должен быть больше 8 символов", Toast.LENGTH_LONG).show()
            }else{
                showLoading(true)
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful){
                        val intent = Intent(this, InputActivity::class.java)
                        startActivity(intent)
                        showLoading(false)
                        finish()
                    }else{
                        Toast.makeText(
                            this,
                            "Что-то пошло не так, попробуйте еще раз",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        }
    }

    private fun showLoading(isLoading: Boolean) {
        // Управление видимостью прогресс-бара и затемнения
        if (isLoading) {
            loadingLayout.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
        } else {
            loadingLayout.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }
}