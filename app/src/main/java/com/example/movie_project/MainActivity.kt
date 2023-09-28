package com.example.movie_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.movie_project.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
import com.example.movie_project.views.FavoritesFragment
import com.example.movie_project.views.ForumFragment
import com.example.movie_project.views.HomeFragment
import com.example.movie_project.views.Leaderboard_Fragment
import com.example.movie_project.views.Search_Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Firestore
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigation()




    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
        fragmentTransaction.commit()

    }

    private fun navigation() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.title) {
                getString(R.string.home) -> replaceFragment(HomeFragment())
                getString(R.string.search) -> replaceFragment(Search_Fragment())
                getString(R.string.leaderboard) -> replaceFragment(Leaderboard_Fragment())
                getString(R.string.favorite) -> replaceFragment(FavoritesFragment())
                getString(R.string.forum) -> replaceFragment(ForumFragment())
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





