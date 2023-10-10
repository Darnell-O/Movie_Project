package com.example.movie_project.views

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.movie_project.ProfileActivity
import com.example.movie_project.R
import com.example.movie_project.databinding.ActivityDetailBinding
import com.example.movie_project.models.MovieModel
import com.example.movie_project.util.getProgressDrawable
import com.example.movie_project.util.loadImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var isFavorite = false
    private lateinit var sharedPreferences: SharedPreferences
    private val BUTTON_STATE_KEY_PREFIX = "button_state_"
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseReference =
        FirebaseDatabase.getInstance().getReference("favorites").child(userId.toString())


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

        favClickListener(movieTitle, movieKey, movie)

    }

    private fun favClickListener(movieTitle: String?, movieKey: String?, movie: MovieModel){
        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val movieSpecificKey = BUTTON_STATE_KEY_PREFIX + movieTitle

        isFavorite = sharedPreferences.getBoolean(movieSpecificKey, false)
        binding.heartButton.isSelected = isFavorite

        if (isFavorite) {
            binding.heartButton.setImageResource(R.drawable.baseline_favorite_24)
        } else {
            binding.heartButton.setImageResource(R.drawable.heart_button)
        }

        binding.heartButton.setOnClickListener {
            val newState = !binding.heartButton.isSelected
            binding.heartButton.isSelected = newState
            sharedPreferences.edit().putBoolean(movieSpecificKey, newState).apply()
            binding.heartButton.setImageResource(R.drawable.baseline_favorite_24)
            isFavorite = sharedPreferences.getBoolean(BUTTON_STATE_KEY_PREFIX + movieTitle, false)
            binding.heartButton.isSelected = isFavorite

            if (newState) {
                binding.heartButton.setImageResource(R.drawable.baseline_favorite_24)
                saveFavToFirebase(movieKey, movie)
            } else {
                binding.heartButton.setImageResource(R.drawable.heart_button)
                removeFavFromFirebase(movieKey)
            }
        }
    }
    private fun saveFavToFirebase(movieKey: String?, movie: MovieModel) {
        if (userId != null && movieKey != null) {
            databaseReference.child(movieKey).setValue(movie)
                .addOnCompleteListener { task ->
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
    }

    private fun removeFavFromFirebase(movieKey: String?) {
        if (userId != null && movieKey != null) {
            databaseReference.child(movieKey).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Failed to remove from Favorites", Toast.LENGTH_SHORT)
                            .show()
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
