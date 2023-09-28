package com.example.movie_project.views

import android.view.View
import com.example.movie_project.models.MovieModel

interface MovieClickListener {
    fun onMovieClicked(movie: MovieModel)

}