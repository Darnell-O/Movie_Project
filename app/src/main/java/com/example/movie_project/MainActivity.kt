package com.example.movie_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.movie_project.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
import com.example.movie_project.views.FavoritesFragment
import com.example.movie_project.views.HomeFragment
import com.example.movie_project.views.movielog.MovieLogFragment
import com.example.movie_project.views.search.SearchFragment
import com.example.movie_project.util.HapticUtil
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firebase initialization + persistence are configured in MovieMagicApp.onCreate
        // BEFORE any FirebaseDatabase.getInstance() call so the instance is not frozen.

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyFloatingNavInsets()
        navigation()




    }

    /**
     * Ensures the floating bottom navigation bar sits above the system gesture /
     * navigation bar on edge-to-edge devices by adding the system bar inset to
     * the view's bottom margin (on top of the design-time 16dp margin).
     */
    private fun applyFloatingNavInsets() {
        val baseMarginPx = binding.bottomNavigation.let {
            (it.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = baseMarginPx + systemBars.bottom
            }
            insets
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
        fragmentTransaction.commit()

    }

    private fun navigation() {
        binding.bottomNavigation.setOnItemSelectedListener {
            HapticUtil.performClickFeedback(binding.bottomNavigation)
            when (it.title) {
                getString(R.string.home) -> replaceFragment(HomeFragment())
                getString(R.string.search) -> replaceFragment(SearchFragment())
                getString(R.string.favorite) -> replaceFragment(FavoritesFragment())
                getString(R.string.movie_log) -> replaceFragment(MovieLogFragment())
                else -> {

                }
            }

            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
