package com.example.movie_project.views.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.movie_project.data.repository.FavoritesRepository
import com.example.movie_project.databinding.ActivityProfileBinding
import com.example.movie_project.views.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    @Inject lateinit var favoritesRepository: FavoritesRepository

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.profileBackButton.setOnClickListener { onBackPressed() }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.logoutButton.setOnClickListener {
            performSignOut()
        }
    }

    /**
     * Sign-out cleanup:
     *  1) Try to flush any pending favorites sync to Firebase (best-effort).
     *  2) Stop the real-time Firebase listener.
     *  3) Clear the local Room cache for this user (privacy on shared devices).
     *  4) Sign out of Firebase Auth and navigate to login.
     */
    private fun performSignOut() {
        val uid = firebaseAuth.currentUser?.uid

        lifecycleScope.launch {
            try {
                if (uid != null) {
                    // Best-effort flush of any queued offline writes
                    favoritesRepository.pushPendingToFirebase(uid)
                    favoritesRepository.stopFirebaseListener()
                    favoritesRepository.clearLocalForUser(uid)
                }
            } finally {
                firebaseAuth.signOut()
                Toast.makeText(this@ProfileActivity, "Good bye!!", Toast.LENGTH_LONG).show()
                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                finish()
                startActivity(intent)
            }
        }
    }
}
