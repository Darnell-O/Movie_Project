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
import com.example.movie_project.databinding.FragmentFavoritesBinding
import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.views.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment(), MovieClickListener {

    private val favViewModel: FavoritesViewModel by viewModels()
    private val favMovieListAdapter = FavoritesAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        binding.favoritesRecyclerView.adapter = favMovieListAdapter
        binding.toolbarFavoritesActivity.title = "Favorites"
        binding.toolbarFavoritesActivity.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbarProfileImage.setOnClickListener {
            startActivity(Intent(activity, ProfileActivity::class.java))
        }
        favMovieListAdapter.setClickListener(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favViewModel.uiState.collect { state ->
                    binding.favoritesProgressBar.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE
                    favMovieListAdapter.updateMovieList(state.favorites)
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
