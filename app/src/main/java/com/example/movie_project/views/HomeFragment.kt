package com.example.movie_project.views

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
import com.example.movie_project.databinding.FragmentHomeBinding
import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.views.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), MovieClickListener {

    private val viewModel: HomeViewModel by viewModels()
    private val movieListAdapter = MovieListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.toolbarHomeActivity.title = "Popular Movies"
        binding.toolbarProfileImage.setOnClickListener {
            startActivity(Intent(activity, ProfileActivity::class.java))
        }

        binding.recyclerView.adapter = movieListAdapter
        movieListAdapter.setClickListener(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.homeProgressBar.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE
                    movieListAdapter.updateMovieList(state.movies)
                    state.errorMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        return binding.root
    }

    override fun onMovieClicked(movie: MovieModel) {
        Toast.makeText(context, "${movie.title}", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, DetailActivity::class.java)
        val bundle = Bundle()
        movie.id.let { bundle.putInt("itemId", it) }
        movie.title?.let { bundle.putString("itemTitle", it) }
        movie.poster?.let { bundle.putString("itemPoster", it) }
        movie.poster_path?.let { bundle.putString("itemPosterPath", it) }
        movie.overview?.let { bundle.putString("itemOverview", it) }
        movie.voteAverage?.let { bundle.putFloat("itemVoteAverage", it) }
        movie.release_date?.let { bundle.putString("itemReleaseDate", it) }
        intent.putExtras(bundle)
        startActivity(intent)
    }
}
