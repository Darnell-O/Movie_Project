package com.example.movie_project

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.movie_project.databinding.ActivitySplashBinding
import com.example.movie_project.databinding.ActivityUsersBinding
import com.google.android.material.animation.AnimationUtils

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_TIME: Long = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler().postDelayed({
            startActivity(Intent(this, Login_Activity::class.java))
            finish()
        }, SPLASH_TIME)
    }
}