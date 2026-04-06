package com.example.movie_project.views.movielog

import com.example.movie_project.data.local.MovieLogEntry

/**
 * Click listener interface for Movie Log RecyclerView items.
 */
interface MovieLogClickListener {
    fun onMovieLogClicked(entry: MovieLogEntry)
}
