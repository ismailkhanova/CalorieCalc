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
import com.example.caloriecalc.InputActivity
import com.example.caloriecalc.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingLayout: FrameLayout
    private val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val emailInput:EditText = findViewById(R.id.email_input)
        val userName: EditText = findViewById(R.id.username_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val passwordConfirm: EditText = findViewById(R.id.password_confirm)
        val signUpBtn: Button = findViewById(R.id.register_button)
        val logInLink: TextView = findViewById(R.id.login_link)

        progressBar = findViewById(R.id.progress_bar)
        loadingLayout = findViewById(R.id.loading_layout)

        logInLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signUpBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val name = userName.text.toString()
            val password = passwordInput.text.toString()
            val passwordConf = passwordConfirm.text.toString()


            if(name.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConf.isEmpty()){
               if(name.isEmpty()){
                   userName.error="Введите имя пользователя"
               }
                if(email.isEmpty()){
                    emailInput.error="Введите верный email-адрес"
                }
                if (password.isEmpty()){
                    passwordInput.error="Введите пароль"
                }
                if (passwordConf.isEmpty()){
                    passwordConfirm.error="Пароли не совпадают"
                }
                Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_LONG).show()
            }
            else if(!email.matches(emailPattern.toRegex())){
                emailInput.error="Некорректный email-адрес"
            }

            else if(password.length<8){
                passwordInput.error="Пароль должен быть больше 8 символов"
                Toast.makeText(this, "Пароль должен быть больше 8 символов", Toast.LENGTH_LONG).show()
            }
            else if(password!=passwordConf){
                passwordConfirm.error="Пароль не совпадает"
                Toast.makeText(this, "Пароль не совпадает", Toast.LENGTH_LONG).show()
            } else{
                showLoading(true)
               auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {

                   if(it.isSuccessful){
                       val database =
                           database.reference.child("users").child(auth.currentUser!!.uid)
                       val users:Users=Users(name,email, auth.currentUser!!.uid)
                       database.setValue(users).addOnCompleteListener {
                           if(it.isSuccessful){
                               val intent=Intent(this, InputActivity::class.java)
                               startActivity(intent)
                               showLoading(false)
                               finish()
                           }
                           else{
                               showLoading(false)
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