package com.example.caloriecalc.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.caloriecalc.fragments.RecipeFragment
import com.example.caloriecalc.fragments.SearchProductFragment

class VpAdapter(fragment: Fragment, private val mealName: String) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SearchProductFragment().apply {
                arguments = Bundle().apply {
                    putString("meal_name", mealName) // Pass meal_name to identify ViewPager context
                }
            }
            1 -> RecipeFragment().apply {
                arguments = Bundle().apply {
                    putString("source", "from_meal")
                    putString("meal_name", mealName)
                }
            }
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}