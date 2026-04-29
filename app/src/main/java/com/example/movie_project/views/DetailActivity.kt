package com.example.movie_project.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.movie_project.MovieMagicApp
import com.example.movie_project.ProfileActivity
import com.example.movie_project.R
import com.example.movie_project.databinding.ActivityDetailBinding
import com.example.movie_project.models.MovieModel
import com.example.movie_project.util.HapticUtil
import com.example.movie_project.util.getProgressDrawable
import com.example.movie_project.util.loadImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * DetailActivity now persists favorites through [FavoritesRepository], which writes
 * to Room first (offline-safe) and pushes to Firebase when online. Heart-button
 * state is driven by the live [isFavorite] LiveData from Room — accurate across
 * devices and offline.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var isFavorite = false
    private var userId: String? = null
    private val repository by lazy { MovieMagicApp.from(this).favoritesRepository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please sign in to manage favorites", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarDetailActivity.setNavigationOnClickListener { finish() }
        binding.toolbarDetailActivity.title = "Movie Details"
        binding.toolbarProfileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Support receiving movie as Serializable (preferred) or via individual extras (legacy)
        val movie = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("movie", MovieModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("movie") as? MovieModel
        }) ?: MovieModel(
            id = intent.getIntExtra("itemId", 0),
            title = intent.getStringExtra("itemTitle"),
            overview = intent.getStringExtra("itemOverview"),
            poster_path = intent.getStringExtra("itemPosterPath"),
            voteAverage = intent.getFloatExtra("itemVoteAverage", 0.0f),
            release_date = intent.getStringExtra("itemReleaseDate")
        )

        binding.toolbarDetailActivity.title = movie.title
        binding.detailImageView.loadImage(movie.poster_path, getProgressDrawable(this))
        binding.tvOverviewDetailActivity.text = movie.overview
        binding.tvTitleDetailActivity.text = movie.title

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("MM-dd-yyyy", Locale.US)
        val formattedDate = try {
            val date = inputFormat.parse(movie.release_date ?: "")
            outputFormat.format(date!!)
        } catch (e: Exception) {
            movie.release_date ?: "N/A"
        }
        binding.tvReleaseDateDetailActivity.text = formattedDate

        setupFavoriteButton(movie)
    }

    private fun setupFavoriteButton(movie: MovieModel) {
        val uid = userId ?: return

        // Observe the live "is this a favorite?" state from Room.
        repository.isFavorite(uid, movie.id).observe(this) { fav ->
            isFavorite = fav
            binding.heartButton.isSelected = fav
            binding.heartButton.setImageResource(
                if (fav) R.drawable.baseline_favorite_24 else R.drawable.heart_button
            )
        }

        binding.heartButton.setOnClickListener { view ->
            HapticUtil.performClickFeedback(view)
            // Toggle through repository — handles online/offline + queue.
            lifecycleScope.launch {
                if (isFavorite) {
                    repository.removeFavorite(uid, movie.id)
                    Toast.makeText(this@DetailActivity, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                } else {
                    repository.addFavorite(uid, movie)
                    Toast.makeText(this@DetailActivity, "Added to Favorites", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}