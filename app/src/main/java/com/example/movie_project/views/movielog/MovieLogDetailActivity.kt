package com.example.movie_project.views.movielog

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.movie_project.R
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.databinding.ActivityMovieLogDetailBinding
import com.example.movie_project.util.HapticUtil
import java.util.UUID

/**
 * Activity for creating or editing a movie log entry.
 * Displays a form with text fields, star rating, checkboxes, and notes.
 */
class MovieLogDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
    }

    private lateinit var binding: ActivityMovieLogDetailBinding
    private val viewModel: MovieLogDetailViewModel by viewModels()

    private var currentRating = 0
    private var editEntryId: String? = null
    private var editEntryDateAdded: Long = System.currentTimeMillis()
    private lateinit var starViews: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieLogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupStarRating()
        setupSaveButton()

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Check if editing an existing entry (entryId is now a String UUID)
        editEntryId = intent.getStringExtra(EXTRA_ENTRY_ID)
        editEntryId?.let { loadExistingEntry(it) }
    }

    /**
     * Sets up the toolbar with back navigation.
     */
    private fun setupToolbar() {
        binding.toolbarMovieLogDetail.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Sets up the 5-star rating system.
     * Clicking a star fills it and all stars to its left in yellow;
     * stars to the right show a yellow outline.
     */
    private fun setupStarRating() {
        starViews = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )

        starViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                HapticUtil.performClickFeedback(it)
                currentRating = index + 1
                updateStarDisplay(currentRating)
            }
        }
    }

    /**
     * Updates the star display based on the current rating.
     * Filled stars (yellow) up to the rating; outlined stars after.
     */
    private fun updateStarDisplay(rating: Int) {
        starViews.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.setImageResource(R.drawable.ic_star_filled)
            } else {
                imageView.setImageResource(R.drawable.ic_star_outline)
            }
        }
    }

    /**
     * Sets up the save button to create or update a movie log entry.
     */
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            HapticUtil.performClickFeedback(it)
            val movieTitle = binding.etMovieTitle.text.toString().trim()

            if (movieTitle.isEmpty()) {
                Toast.makeText(this, "Please enter a movie title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Build entry with userId placeholder (repository will set the real userId)
            val entry = MovieLogEntry(
                userId = "",  // Will be set by repository
                entryId = editEntryId ?: UUID.randomUUID().toString(),
                movieTitle = movieTitle,
                year = binding.etYear.text.toString().trim(),
                dateWatched = binding.etDateWatched.text.toString().trim(),
                directedBy = binding.etDirectedBy.text.toString().trim(),
                starring = binding.etStarring.text.toString().trim(),
                rating = currentRating,
                inTheater = binding.cbInTheater.isChecked,
                atHome = binding.cbAtHome.isChecked,
                firstWatch = binding.cbFirstWatch.isChecked,
                rewatch = binding.cbRewatch.isChecked,
                alone = binding.cbAlone.isChecked,
                withSomeone = binding.cbWithSomeone.isChecked,
                notes = binding.etNotes.text.toString().trim(),
                dateAdded = if (editEntryId != null) editEntryDateAdded else System.currentTimeMillis()
            )

            if (editEntryId != null) {
                viewModel.updateEntry(entry) {
                    runOnUiThread {
                        Toast.makeText(this, "Movie updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                viewModel.insertEntry(entry) {
                    runOnUiThread {
                        Toast.makeText(this, "Movie saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    /**
     * Loads an existing entry for editing and populates the form fields.
     */
    private fun loadExistingEntry(entryId: String) {
        viewModel.getEntryById(entryId).observe(this) { entry ->
            entry?.let {
                editEntryDateAdded = it.dateAdded
                binding.etMovieTitle.setText(it.movieTitle)
                binding.etYear.setText(it.year)
                binding.etDateWatched.setText(it.dateWatched)
                binding.etDirectedBy.setText(it.directedBy)
                binding.etStarring.setText(it.starring)
                currentRating = it.rating
                updateStarDisplay(currentRating)
                binding.cbInTheater.isChecked = it.inTheater
                binding.cbAtHome.isChecked = it.atHome
                binding.cbFirstWatch.isChecked = it.firstWatch
                binding.cbRewatch.isChecked = it.rewatch
                binding.cbAlone.isChecked = it.alone
                binding.cbWithSomeone.isChecked = it.withSomeone
                binding.etNotes.setText(it.notes)
            }
        }
    }
}
