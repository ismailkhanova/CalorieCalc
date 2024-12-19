package com.example.caloriecalc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.caloriecalc.databinding.ActivityMainBinding
import com.example.caloriecalc.databinding.ActivityMainScreenBinding
import com.example.caloriecalc.fragments.DiaryFragment
import com.example.caloriecalc.fragments.ProfileFragment
import com.example.caloriecalc.fragments.RecipeFragment
import com.example.caloriecalc.fragments.StatisticFragment
import com.example.caloriecalc.ui.theme.CalorieCalcTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainScreenBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainScreenBinding.inflate(layoutInflater)
            setContentView(binding.root)
            if (savedInstanceState == null) {
                replaceFragment(ProfileFragment())
                binding.bottomNavigation.selectedItemId = R.id.nav_profile
            }

            binding.bottomNavigation.setOnItemSelectedListener {
                when(it.itemId){
                    R.id.nav_diary -> replaceFragment(DiaryFragment())
                    R.id.nav_book -> replaceFragment(RecipeFragment())
                    R.id.nav_stats -> replaceFragment(StatisticFragment())
                    R.id.nav_profile -> replaceFragment(ProfileFragment())

                    else ->{

                    }
                }
                true
            }
        }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container,fragment)
        fragmentTransaction.commit()
    }
    }
