package com.example.movie_project.views

import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.example.movie_project.MainActivity
import com.example.movie_project.R
import com.example.movie_project.databinding.FragmentSearchBinding
import com.example.movie_project.models.MovieModel

class Search_Fragment : Fragment(), MovieClickListener {

    private val searchViewModel: SearchViewModel by viewModels()
    private val searchAdapter = SearchAdapter(arrayListOf())
    private lateinit var binding: FragmentSearchBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.toolbarSearchActivity.title = "Search"
        binding.toolbarProfileImage.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.lifecycleOwner = this
        binding.searchRecyclerView.adapter = searchAdapter
        searchAdapter.setClickListener(this)
        binding.searchView.clearFocus()
       // searchViewModel.searchMovies(query(binding.searchView.query.toString()))


        searchViewModel.searchMovies.observe(viewLifecycleOwner) { movies ->
            movies.forEach {
                println(it.title)
                Log.i("SearchFragment", "Movie: ${it.title}")
            }
            movies?.let {
                searchAdapter.updateMovieList(it)
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchViewModel.searchMovies(query)
                } else {
                    Toast.makeText(context, "Please enter a search query", Toast.LENGTH_SHORT)
                        .show()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })




        return binding.root

    }

//      fun query(text:String):String {
//      binding.searchView.setOnQueryTextListener(object :
//          SearchView.OnQueryTextListener {
//          override fun onQueryTextSubmit(text: String?): Boolean {
//              filterList(text.toString())
//              return false
//          }
//
//          override fun onQueryTextChange(newText: String?): Boolean {
//              filterList(newText.toString())
//              return false
//          }
//      })
//
//      return binding.searchView.query.toString()

//  }
//
//    fun filterList(text: String) {
//        val filteredList = ArrayList<MovieModel>()
//        for (item in searchViewModel.searchMovies.value!!) {
//            if (item.title?.toLowerCase()?.contains(text.toLowerCase()) == true) {
//                filteredList.add(item)
//            }
//        }
//        searchAdapter.filterList(filteredList)
//
//        if (filteredList.isEmpty()) {
//            Toast.makeText(context, "No Results Found", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(context, "Results Found", Toast.LENGTH_SHORT).show()
//        }
//    }

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

//     fun searchMovies(query: String) {
//        // Your searchMovies function logic here
//        viewModel.searchMovies(query)
//    }
}