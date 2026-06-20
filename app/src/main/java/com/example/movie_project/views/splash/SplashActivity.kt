package com.example.movie_project.views.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.movie_project.views.auth.LoginActivity
import com.example.movie_project.views.theme.MovieMagicTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieMagicTheme {
                SplashScreen(
                    onTimeout = {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}