package com.example.movie_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.movie_project.databinding.ActivityLogin1Binding
import com.google.firebase.auth.FirebaseAuth

class Login_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLogin1Binding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        emailFocusListener()
        loginPasswordFocusListener()

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signupButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val loginUserName = binding.loginUserEditText.text.toString()
            val loginPassword = binding.loginPasswordEditText.text.toString()

            if (loginUserName.isNotEmpty() && loginPassword.isNotEmpty() && loginValidPassword() == null && validEmail() == null) {
                firebaseAuth.signInWithEmailAndPassword(loginUserName, loginPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            finish()
                            startActivity(intent)
                            Toast.makeText(this, "Welcome back $loginUserName", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            } else {
                Toast.makeText(this, "Some field are empty", Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Welcome back", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun emailFocusListener() {
        binding.loginUserEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.loginUsernameContainer.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val email = binding.loginUserEditText.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email"
        }
        return null
    }

    private fun loginPasswordFocusListener() {
        binding.loginPasswordEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.passwordTextInput.helperText = loginValidPassword()
            }
        }
    }

    private fun loginValidPassword(): String? {
        val password = binding.loginPasswordEditText.text.toString()
        when {
            password.isEmpty() -> {
                return "Password is required"
            }

            password.length < 6 -> {
                return "Password must be at least 6 characters"
            }

            !password.matches(Regex(".*[A-Z].*")) -> {
                return "Password must contain at least one upper-case letter"
            }

            !password.matches(Regex(".*[a-z].*")) -> {
                return "Password must contain at least one lower-case letter"
            }

            else ->

                return null
        }
    }
}




