package com.example.movie_project.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.movie_project.Login_Activity
import com.example.movie_project.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(this,"Good bye!!", Toast.LENGTH_LONG)
                .show()
            val intent = Intent(this, Login_Activity::class.java)
            finish()
            startActivity(intent)
        }
    }
}