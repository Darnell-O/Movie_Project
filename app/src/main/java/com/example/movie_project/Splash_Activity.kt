package com.example.movie_project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.movie_project.views.splash.SplashScreen
import com.example.movie_project.views.theme.MovieMagicTheme

class Splash_Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieMagicTheme {
                SplashScreen(
                    onTimeout = {
                        startActivity(Intent(this@Splash_Activity, Login_Activity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}