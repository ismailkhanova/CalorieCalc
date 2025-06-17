package com.example.caloriecalc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.caloriecalc.R
import com.example.caloriecalc.data.ProfileViewModel
import com.example.caloriecalc.data.UserProfile

class MyDataFragment : Fragment() {

    private lateinit var spinnerGoal: Spinner
    private lateinit var editTargetCalories: EditText
    private lateinit var editWeight: EditText
    private lateinit var textHeight: TextView
    private lateinit var spinnerActivity: Spinner
    private lateinit var buttonSave: Button
    private lateinit var buttonRecalculate: Button
    private lateinit var editAge: EditText
    private lateinit var radioGroupGender: RadioGroup

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_data, container, false)

        spinnerGoal = view.findViewById(R.id.spinner_goal)
        editTargetCalories = view.findViewById(R.id.edit_target_calories)
        editWeight = view.findViewById(R.id.edit_weight)
        textHeight = view.findViewById<TextView>(R.id.text_height) // Explicitly specify TextView type
        spinnerActivity = view.findViewById(R.id.spinner_activity)
        buttonSave = view.findViewById(R.id.button_save)
        buttonRecalculate = view.findViewById(R.id.button_recalculate)
        editAge = view.findViewById(R.id.edit_age)
        radioGroupGender = view.findViewById(R.id.radio_group_gender)

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                editAge.setText(it.age?.toString() ?: "")
                if (it.gender == "Мужчина") {
                    radioGroupGender.check(R.id.radio_male)
                } else {
                    radioGroupGender.check(R.id.radio_female)
                }
                spinnerGoal.setSelection(getSpinnerIndex(spinnerGoal, it.goal ?: ""))
                editTargetCalories.setText(it.goalCalories?.toString() ?: "")
                editWeight.setText(it.weight?.toString() ?: "")
                textHeight.text = it.height?.toString() ?: ""
                spinnerActivity.setSelection(getSpinnerIndex(spinnerActivity, activityLevelToString(it.activityLevel)))
            }
        }

        profileViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        profileViewModel.loadUserData()

        buttonSave.setOnClickListener {
            val updatedProfile = UserProfile(
                height = textHeight.text.toString().toIntOrNull(),
                weight = editWeight.text.toString().toDoubleOrNull(),
                age = editAge.text.toString().toIntOrNull(),
                gender = if (radioGroupGender.checkedRadioButtonId == R.id.radio_male) "Мужчина" else "Женщина",
                goal = spinnerGoal.selectedItem.toString(),
                goalCalories = editTargetCalories.text.toString().toIntOrNull(),
                activityLevel = stringToActivityLevel(spinnerActivity.selectedItem.toString())
            )
            profileViewModel.saveUserData(updatedProfile)

            Toast.makeText(requireContext(), "Данные успешно сохранены", Toast.LENGTH_SHORT).show()
        }

        buttonRecalculate.setOnClickListener {
            val updatedProfile = UserProfile(
                height = textHeight.text.toString().toIntOrNull(),
                weight = editWeight.text.toString().toDoubleOrNull(),
                age = editAge.text.toString().toIntOrNull(),
                gender = if (radioGroupGender.checkedRadioButtonId == R.id.radio_male) "Мужчина" else "Женщина",
                goal = spinnerGoal.selectedItem.toString(),
                activityLevel = stringToActivityLevel(spinnerActivity.selectedItem.toString())
            )

            val recalculatedCalories = profileViewModel.recalculateTargetCalories(updatedProfile)
            editTargetCalories.setText(recalculatedCalories.toString())
            Toast.makeText(requireContext(), "Калории пересчитаны", Toast.LENGTH_SHORT).show()
        }


        return view
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