package com.example.movie_project.views.movielog

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.movie_project.R
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.databinding.ActivityMovieLogDetailBinding
import com.example.movie_project.util.HapticUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
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
        observeState()
        observeEvents()

        editEntryId = intent.getStringExtra(EXTRA_ENTRY_ID)
        editEntryId?.let { viewModel.loadEntry(it) }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.errorMessage?.let {
                        Toast.makeText(this@MovieLogDetailActivity, it, Toast.LENGTH_LONG).show()
                    }
                    state.entry?.let { entry ->
                        if (editEntryId != null) populateForm(entry)
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is MovieLogDetailEvent.Saved -> {
                            Toast.makeText(this@MovieLogDetailActivity, "Movie saved!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is MovieLogDetailEvent.Deleted -> {
                            Toast.makeText(this@MovieLogDetailActivity, "Entry deleted!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun populateForm(entry: MovieLogEntry) {
        editEntryDateAdded = entry.dateAdded
        binding.etMovieTitle.setText(entry.movieTitle)
        binding.etYear.setText(entry.year)
        binding.etDateWatched.setText(entry.dateWatched)
        binding.etDirectedBy.setText(entry.directedBy)
        binding.etStarring.setText(entry.starring)
        currentRating = entry.rating
        updateStarDisplay(currentRating)
        binding.cbInTheater.isChecked = entry.inTheater
        binding.cbAtHome.isChecked = entry.atHome
        binding.cbFirstWatch.isChecked = entry.firstWatch
        binding.cbRewatch.isChecked = entry.rewatch
        binding.cbAlone.isChecked = entry.alone
        binding.cbWithSomeone.isChecked = entry.withSomeone
        binding.etNotes.setText(entry.notes)
    }

    private fun setupToolbar() {
        binding.toolbarMovieLogDetail.setNavigationOnClickListener { finish() }
    }

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

    private fun updateStarDisplay(rating: Int) {
        starViews.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index < rating) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            HapticUtil.performClickFeedback(it)
            val movieTitle = binding.etMovieTitle.text.toString().trim()

            if (movieTitle.isEmpty()) {
                Toast.makeText(this, "Please enter a movie title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val entry = MovieLogEntry(
                userId = "",
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
                viewModel.updateEntry(entry)
            } else {
                viewModel.insertEntry(entry)
            }
        }
    }
}
