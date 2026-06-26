package com.example.movie_project.views

import com.example.movie_project.models.domain.MovieModel

interface MovieClickListener {
    fun onMovieClicked(movie: MovieModel)


}