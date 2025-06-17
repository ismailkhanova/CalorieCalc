package com.example.caloriecalc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.caloriecalc.R
import com.example.caloriecalc.data.ProfileViewModel
import com.example.caloriecalc.data.UserProfile
import com.example.caloriecalc.databinding.FragmentMyDataBinding

class MyDataFragment : Fragment() {

    private var _binding: FragmentMyDataBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyDataBinding.inflate(inflater, container, false)
        val view = binding.root

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding.buttonBack.setOnClickListener{
            findNavController().navigateUp()
        }

        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.editAge.setText(it.age?.toString() ?: "")
                if (it.gender == "Мужчина") {
                    binding.radioGroupGender.check(R.id.radio_male)
                } else {
                    binding.radioGroupGender.check(R.id.radio_female)
                }
                binding.spinnerGoal.setSelection(getSpinnerIndex(binding.spinnerGoal, it.goal ?: ""))
                binding.editTargetCalories.setText(it.goalCalories?.toString() ?: "")
                binding.editWeight.setText(it.weight?.toString() ?: "")
                binding.textHeight.setText(it.height?.toString() ?: "") // Исправлено
                binding.spinnerActivity.setSelection(
                    getSpinnerIndex(binding.spinnerActivity, activityLevelToString(it.activityLevel))
                )
            }
        }

        profileViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        profileViewModel.loadUserData()

        binding.buttonSave.setOnClickListener {
            val updatedProfile = UserProfile(
                height = binding.textHeight.text.toString().toIntOrNull(),
                weight = binding.editWeight.text.toString().toDoubleOrNull(),
                age = binding.editAge.text.toString().toIntOrNull(),
                gender = if (binding.radioGroupGender.checkedRadioButtonId == R.id.radio_male) "Мужчина" else "Женщина",
                goal = binding.spinnerGoal.selectedItem.toString(),
                goalCalories = binding.editTargetCalories.text.toString().toIntOrNull(),
                activityLevel = stringToActivityLevel(binding.spinnerActivity.selectedItem.toString())
            )
            profileViewModel.saveUserData(updatedProfile)

            Toast.makeText(requireContext(), "Данные успешно сохранены", Toast.LENGTH_SHORT).show()
        }

        binding.buttonRecalculate.setOnClickListener {
            val updatedProfile = UserProfile(
                height = binding.textHeight.text.toString().toIntOrNull(),
                weight = binding.editWeight.text.toString().toDoubleOrNull(),
                age = binding.editAge.text.toString().toIntOrNull(),
                gender = if (binding.radioGroupGender.checkedRadioButtonId == R.id.radio_male) "Мужчина" else "Женщина",
                goal = binding.spinnerGoal.selectedItem.toString(),
                activityLevel = stringToActivityLevel(binding.spinnerActivity.selectedItem.toString())
            )

            val recalculatedCalories = profileViewModel.recalculateTargetCalories(updatedProfile)
            binding.editTargetCalories.setText(recalculatedCalories.toString())
            Toast.makeText(requireContext(), "Калории пересчитаны", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i) == value) {
                return i
            }
        }
        return 0
    }

    private fun activityLevelToString(activityLevel: Double?): String {
        return when (activityLevel) {
            1.2 -> "Нет активности"
            1.375 -> "Легкая активность"
            1.55 -> "Умеренная активность"
            1.725 -> "Интенсивная активность"
            1.9 -> "Очень высокая активность"
            else -> "Нет активности"
        }
    }

    private fun stringToActivityLevel(activity: String): Double? {
        return when (activity) {
            "Нет активности" -> 1.2
            "Легкая активность" -> 1.375
            "Умеренная активность" -> 1.55
            "Интенсивная активность" -> 1.725
            "Очень высокая активность" -> 1.9
            else -> null
        }
    }
}