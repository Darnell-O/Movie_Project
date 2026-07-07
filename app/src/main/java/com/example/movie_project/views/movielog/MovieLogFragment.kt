package com.example.movie_project.views.movielog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.databinding.FragmentMovieLogBinding
import com.example.movie_project.util.HapticUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
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

        movieLogAdapter = MovieLogAdapter(arrayListOf())
        movieLogAdapter.setClickListener(this)
        binding.movieLogRecyclerView.adapter = movieLogAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val isEmpty = state.entries.isEmpty()
                    binding.tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                    binding.movieLogRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
                    if (!isEmpty) movieLogAdapter.updateEntries(state.entries)
                    state.errorMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.fabAddMovieLog.setOnClickListener {
            HapticUtil.performClickFeedback(it)
            startActivity(Intent(activity, MovieLogDetailActivity::class.java))
        }

        return binding.root
    }

    override fun onMovieLogClicked(entry: MovieLogEntry) {
        val intent = Intent(activity, MovieLogDetailActivity::class.java)
        intent.putExtra(MovieLogDetailActivity.EXTRA_ENTRY_ID, entry.entryId)
        startActivity(intent)
    }
}
