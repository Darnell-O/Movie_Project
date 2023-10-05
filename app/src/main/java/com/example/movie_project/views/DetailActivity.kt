package com.example.movie_project.views

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.movie_project.R
import com.example.movie_project.databinding.ActivityDetailBinding
import com.example.movie_project.databinding.ActivityMainBinding
import com.example.movie_project.models.MovieModel
import com.example.movie_project.util.getProgressDrawable
import com.example.movie_project.util.loadImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var isFavorite = false
    private lateinit var sharedPreferences: SharedPreferences
    private val BUTTON_STATE_KEY = "button_state"
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("favorites")
    private val databaseReference = FirebaseDatabase.getInstance().getReference("favorites").child(userId.toString())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarDetailActivity.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarDetailActivity.title = "Movie Details"
        binding.toolbarProfileImage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val movieId = intent.getIntExtra("itemId", 0)
        val movieTitle = intent.getStringExtra("itemTitle")
        val movieOverview = intent.getStringExtra("itemOverview")
        val moviePosterPath = intent.getStringExtra("itemPosterPath")
        val movieReleaseDate = intent.getStringExtra("itemReleaseDate")
        val movieVoteAverage = intent.getFloatExtra("itemVoteAverage", 0.0f)

        val movieKey = movieId.toString()
        val movie = MovieModel(
            movieId,
            movieTitle,
            movieOverview,
            moviePosterPath,
            movieVoteAverage,
            movieReleaseDate
        )

        binding.toolbarDetailActivity.title = movieTitle
        binding.detailImageView.loadImage(moviePosterPath, getProgressDrawable(this))
        binding.tvOverviewDetailActivity.text = movieOverview
        binding.tvTitleDetailActivity.text = movieTitle
        binding.tvReleaseDateDetailActivity.text = movieReleaseDate
//        binding.tvVoteAverageDetailActivity.text = movieVoteAverage.toString()


        binding.heartButton.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                binding.heartButton.setImageResource(R.drawable.baseline_favorite_24)
                if (userId != null && movieKey != null) {
                    databaseReference.child(movieKey).setValue(movie).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to add to Favorites",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }

                }
            } else {
                binding.heartButton.setImageResource(R.drawable.heart_button)
                val unFavUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (unFavUserId != null) {
                    val unFavMovie_Id = movieTitle.toString()
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    databaseReference.child(unFavUserId).child("favorites")
                        .child(unFavMovie_Id).removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to remove from Favorites",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }

                }
            }
        }


    }


    enum class Rating {
        OKAY,
        GOOD,
        GREAT
    }
}