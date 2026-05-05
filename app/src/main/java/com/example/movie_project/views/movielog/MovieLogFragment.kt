package com.example.movie_project.views.movielog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.databinding.FragmentMovieLogBinding
import com.example.movie_project.util.HapticUtil

/**
 * Fragment displaying the Movie Log list.
 * Shows all logged movies in a RecyclerView with a FAB to add new entries.
 */
class MovieLogFragment : Fragment(), MovieLogClickListener {

    private val viewModel: MovieLogViewModel by viewModels()
    private lateinit var movieLogAdapter: MovieLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMovieLogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        // Setup RecyclerView adapter
        movieLogAdapter = MovieLogAdapter(arrayListOf())
        movieLogAdapter.setClickListener(this)
        binding.movieLogRecyclerView.adapter = movieLogAdapter

        // Observe movie log entries
        viewModel.allEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNullOrEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.movieLogRecyclerView.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.movieLogRecyclerView.visibility = View.VISIBLE
                movieLogAdapter.updateEntries(entries)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        // FAB click — navigate to detail screen for adding a new entry
        binding.fabAddMovieLog.setOnClickListener {
            HapticUtil.performClickFeedback(it)
            val intent = Intent(activity, MovieLogDetailActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    /**
     * Called when a movie log item is clicked.
     * Navigates to the detail screen for editing using the UUID-based entryId.
     */
    override fun onMovieLogClicked(entry: MovieLogEntry) {
        val intent = Intent(activity, MovieLogDetailActivity::class.java)
        intent.putExtra(MovieLogDetailActivity.EXTRA_ENTRY_ID, entry.entryId)
        startActivity(intent)
    }
}
