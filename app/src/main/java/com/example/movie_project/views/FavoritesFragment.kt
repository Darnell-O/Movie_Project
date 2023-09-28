package com.example.movie_project.views

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.movie_project.R
import com.example.movie_project.databinding.FragmentFavoritesBinding
import com.example.movie_project.models.MovieModel

class FavoritesFragment : Fragment(), MovieClickListener {

    private val favViewModel: FavoritesViewModel by viewModels()
    private val favMovieListAdapter = MovieListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_favorites, container, false)
        val binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        favViewModel.fetchFavorites()
        binding.favoritesRecyclerView.adapter = favMovieListAdapter
        favMovieListAdapter.setClickListener(this)
//        favViewModel.favorites.observe(viewLifecycleOwner) { favorites ->
//            favorites.forEach {
//                println(it.title)
//            }
//
//            favorites?.let {
//                favMovieListAdapter.updateMovieList(it)
//            }
//        }

        return binding.root
    }

    override fun onMovieClicked(movie: MovieModel) {
        Toast.makeText(context, "${movie?.title}", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, DetailActivity::class.java)
        val bundle = Bundle()
        movie?.title?.let { bundle.putString("itemTitle", it) }
        movie?.poster?.let { bundle.putString("itemPoster", it) }
        movie?.poster_path?.let { bundle.putString("itemPosterPath", it) }
        movie?.overview?.let { bundle.putString("itemOverview", it) }
        movie?.voteAverage?.let { bundle.putFloat("itemVoteAverage", it) }
        movie?.release_date?.let { bundle.putString("itemReleaseDate", it) }
        intent.putExtras(bundle)
        startActivity(intent)
    }

}