package com.example.movie_project.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.movie_project.R
import com.example.movie_project.databinding.FragmentFavoritesBinding
import com.example.movie_project.models.MovieModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoritesFragment : Fragment(), MovieClickListener {

    private val favViewModel: FavoritesViewModel by viewModels()
    private val favMovieListAdapter = FavoritesAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.favoritesRecyclerView.adapter = favMovieListAdapter
        binding.toolbarFavoritesActivity.title = "Favorites"
        binding.toolbarFavoritesActivity?.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbarProfileImage.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }
        favMovieListAdapter.setClickListener(this)
        favViewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            favMovieListAdapter.updateMovieList(favorites)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favViewModel.fetchFavorites()
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