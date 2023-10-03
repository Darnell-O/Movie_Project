package com.example.movie_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.movie_project.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emailFocusListener()
        passwordFocusListener()
        confirmPasswordFocusListener()

        firebaseAuth = FirebaseAuth.getInstance()

        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
                getString(R.string.default_web_client_id)
            ).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        signUp()

        binding.googleSignupButton.setOnClickListener {
            signInGoogle()
        }

    }

    fun signUp() {
        binding.signupButton.setOnClickListener {
            val userName = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (userName.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && validEmail() == null && validPassword() == null) {

                if (password == confirmPassword && password.length >= 6 && confirmPassword.length >= 6) {

                    firebaseAuth.createUserWithEmailAndPassword(userName, password)
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful) {
                                /**Add hashmap for storage later*/
                                val intent = Intent(this, Login_Activity::class.java)
                                startActivity(intent)
//                                intent.putExtra("userName", userName)
//                                intent.putExtra("password", password)
//                                setResult(RESULT_OK, intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Password Error + ${it.exception.toString()}",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Some field are empty", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun signInGoogle() {
        val intent = googleSignInClient.signInIntent
        launcher.launch(intent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent: Intent = Intent(this, MainActivity::class.java)
                intent.putExtra("email", account.email)
                intent.putExtra("name", account.displayName)
                startActivity(intent)
            } else {
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun emailFocusListener() {
        binding.usernameEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.usernameContainer.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val email = binding.usernameEditText.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email"
        }
        return null
    }

    private fun passwordFocusListener() {
        binding.passwordEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.passwordTextInput.helperText = validPassword()
            }
        }
    }

    private fun confirmPasswordFocusListener() {
        binding.confirmPasswordEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.ConfirmPasswordTextInput.helperText = validPassword()
            }
        }
    }

    private fun validPassword(): String? {
        val password = binding.passwordEditText.text.toString()
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