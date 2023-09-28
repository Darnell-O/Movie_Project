package com.example.movie_project.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_project.databinding.ItemMovieCardBinding
import com.example.movie_project.models.MovieModel


class MovieListAdapter(
    private val movieList: ArrayList<MovieModel>,
    private var clickListener: MovieClickListener? = null
) :
    RecyclerView.Adapter<MovieListAdapter.MovieViewHolder>() {

    fun setClickListener(clickListener: MovieClickListener) {
        this.clickListener = clickListener
    }

    fun updateMovieList(newMovieList: List<MovieModel>) {
        movieList.clear()
        movieList.addAll(newMovieList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: ItemMovieCardBinding = DataBindingUtil.inflate(
            inflater,
            com.example.movie_project.R.layout.item_movie_card,
            parent,
            false
        )
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movieList[position]
        movie?.let {
            holder.view.movie = it
        }


        holder.view.cardView.setOnClickListener {
            clickListener?.onMovieClicked(movie)

        }

    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    class MovieViewHolder(var view: ItemMovieCardBinding) : RecyclerView.ViewHolder(view.root) {

    }
}

