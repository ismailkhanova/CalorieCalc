package com.example.caloriecalc.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.caloriecalc.R
import com.example.caloriecalc.adapters.VpAdapter
import com.example.caloriecalc.databinding.FragmentAddToMealViewPagerBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class AddToMealViewPagerFragment : Fragment(R.layout.fragment_add_to_meal_view_pager) {
    private var _binding: FragmentAddToMealViewPagerBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddToMealViewPagerBinding.bind(view)

        val mealName = arguments?.getString("meal_name") ?: "Завтрак"

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val cancelBtn = binding.btnCancel

        cancelBtn.setOnClickListener {
            findNavController().navigateUp() // Navigate back to previous fragment
        }

        viewPager.adapter = VpAdapter(this, mealName)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "Продукты"
                1 -> "Рецепты"
                else -> null
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}