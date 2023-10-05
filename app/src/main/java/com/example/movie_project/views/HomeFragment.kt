package com.example.movie_project.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.movie_project.databinding.FragmentHomeBinding
import com.example.movie_project.models.MovieModel
import com.example.movie_project.models.UsersModel


class HomeFragment : Fragment(), MovieClickListener {

    private val viewModel: HomeViewModel by viewModels()
    private val movieListAdapter = MovieListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        viewModel.fetchMovies()
        binding.toolbar.title = "Popular Movies"
//        val activity = activity as AppCompatActivity?
//        activity?.setSupportActionBar(binding.toolbar)
//        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        activity?.supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.recyclerView.adapter = movieListAdapter
        movieListAdapter.setClickListener(this)
        viewModel.movies.observe(viewLifecycleOwner) { movies ->
            movies.forEach {
                println(it.title)
                Log.i("HomeFragment", "Movie: ${it.title}")
            }

            movies?.let {
                movieListAdapter.updateMovieList(it)
            }
        }

        return binding.root
    }

    override fun onMovieClicked(movie: MovieModel) {
        Toast.makeText(context, "${movie?.title}", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, DetailActivity::class.java)
        val bundle = Bundle()
        movie?.id?.let { bundle.putInt("itemId", it) }
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