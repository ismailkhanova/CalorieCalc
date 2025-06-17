package com.example.caloriecalc.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> get() = _userProfile

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun loadUserData() {
        val currentUserUid = auth.currentUser?.uid ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }
        val userRef = database.child("profiles").child(currentUserUid)

        userRef.get().addOnSuccessListener { snapshot ->
            val data = snapshot.getValue(UserProfile::class.java)
            _userProfile.value = data ?: UserProfile() // Assign default empty profile if data is null
        }.addOnFailureListener { exception ->
            _error.value = "Ошибка загрузки данных: ${exception.message}"
        }
    }

    fun recalculateTargetCalories(profile: UserProfile): Int {
        val bmr = if (profile.gender == "Мужчина") {
            10 * (profile.weight ?: 0.0) + 6.25 * (profile.height ?: 0) - 5 * (profile.age ?: 0) + 5
        } else {
            10 * (profile.weight ?: 0.0) + 6.25 * (profile.height ?: 0) - 5 * (profile.age ?: 0) - 161
        }
        return (bmr * (profile.activityLevel ?: 1.2)).toInt()
    }

    fun saveUserData(updatedProfile: UserProfile) {
        val currentUserUid = auth.currentUser?.uid ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        // Recalculate target calories before saving
        updatedProfile.goalCalories = recalculateTargetCalories(updatedProfile)

        val userRef = database.child("profiles").child(currentUserUid)
        userRef.setValue(updatedProfile).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                _error.value = "Ошибка сохранения данных: ${task.exception?.message}"
            }
        }.addOnFailureListener { exception ->
            _error.value = "Ошибка сохранения данных: ${exception.message}"
        }
    }
}