package com.example.caloriecalc.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caloriecalc.R
import com.example.caloriecalc.adapters.RecipeAdapter
import com.example.caloriecalc.data.Recipe
import com.example.caloriecalc.data.RecipeViewModel
import com.example.caloriecalc.databinding.FragmentRecipeBinding
import com.google.firebase.auth.FirebaseAuth


class RecipeFragment : Fragment() {
    private lateinit var binding: FragmentRecipeBinding
    private val viewModel: RecipeViewModel by activityViewModels()
    private lateinit var adapter: RecipeAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addRecipe: Button = view.findViewById(R.id.add_recipe_btn)
        setupRecyclerView()
        observeViewModel()

        addRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_recipeFragment_to_createRecipeFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter =  RecipeAdapter{ recipe ->
            openRecipeDetails(recipe)
        }
        binding.recyclerViewMeals.adapter = adapter
        binding.recyclerViewMeals.layoutManager = LinearLayoutManager(requireContext())
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModel.loadRecipes(userId)

    }

    private fun observeViewModel(){
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
            binding.emptyState.visibility = if(recipes.isEmpty()) View.VISIBLE else View.GONE

        }
    }

    private fun openRecipeDetails(recipe: Recipe){
        val action = RecipeFragmentDirections.actionRecipeFragmentToCreateRecipeFragment(recipe ?: Recipe()) // передаем пустой объект, если `recipe` null
        findNavController().navigate(action)
    }

}