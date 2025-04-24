package com.example.caloriecalc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.caloriecalc.databinding.ActivityMainBinding
import com.example.caloriecalc.databinding.ActivityMainScreenBinding
import com.example.caloriecalc.fragments.DiaryFragment
import com.example.caloriecalc.fragments.ProfileFragment
import com.example.caloriecalc.fragments.RecipeFragment
import com.example.caloriecalc.fragments.StatisticFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainScreenActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        // УБИРАЕМ setupWithNavController() и управляем вручную
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> navController.navigate(R.id.profileFragment)
                R.id.nav_diary -> navController.navigate(R.id.diaryFragment)
                R.id.nav_book -> navController.navigate(R.id.recipeFragment)
                R.id.nav_stats -> navController.navigate(R.id.statisticFragment)
            }
            true
        }

        // При старте открываем профиль и выделяем его
        if (savedInstanceState == null) {
            navController.navigate(R.id.profileFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_profile  // Теперь это сработает
        }
    }
}

