package com.example.caloriecalc.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.caloriecalc.R
import com.example.caloriecalc.registration_user.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val logoutButton: ImageView = view.findViewById(R.id.logout_button)
        val userAvatar: ImageView = view.findViewById(R.id.avatar)
        val username: TextView = view.findViewById(R.id.username)
        val user_email: TextView = view.findViewById(R.id.email)
        val userGoal: TextView = view.findViewById(R.id.goal_label)
        val myData: LinearLayout = view.findViewById(R.id.my_data)
        val notify: LinearLayout = view.findViewById(R.id.reminders)
        val settings: LinearLayout = view.findViewById(R.id.settings)
        val help: LinearLayout = view.findViewById(R.id.help)


        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val savedUserName = sharedPreferences.getString("userName", null)
        val savedEmail = sharedPreferences.getString("email", null)
        val savedGoal = sharedPreferences.getString("goal", null)

        if (!savedUserName.isNullOrEmpty() && !savedEmail.isNullOrEmpty()){
            username.text = savedUserName
            user_email.text = savedEmail
            userGoal.text = savedGoal
        }

            val currentUserUid = auth.currentUser?.uid
            if (currentUserUid != null) {
                val userRef = database.child("users").child(currentUserUid)


                userRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Обновляем имя пользователя
                        val userName = snapshot.child("userName").getValue(String::class.java)
                        if (!userName.isNullOrEmpty()) {
                            username.text = userName
                            editor.putString("userName", userName).apply() // Сохраняем локально
                        }

                        // Обновляем почту
                        val email = snapshot.child("email").getValue(String::class.java)
                        if (!email.isNullOrEmpty()) {
                            user_email.text = email
                            editor.putString("email", email).apply() // Сохраняем локально
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Ошибка загрузки данных: ${error.message}")
                    }
                })

                val profileRef = database.child("profiles").child(currentUserUid)
                profileRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Обновляем цель
                        val goal = snapshot.child("goal").getValue(String::class.java)
                        if (!goal.isNullOrEmpty()) {
                            userGoal.text = "Ваша цель: $goal"
                            editor.putString("goal", goal).apply() // Сохраняем локально
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Ошибка загрузки цели: ${error.message}")
                    }
                })
            }



        logoutButton.setOnClickListener {
            logout()

        }
    }

    private fun logout(){
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}

