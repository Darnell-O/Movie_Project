package com.example.movie_project.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_project.databinding.ItemMovieCardBinding
import com.example.movie_project.models.MovieModel


class FavoritesAdapter(
    val movieList: ArrayList<MovieModel>,
    private var clickListener: MovieClickListener? = null

) :
    RecyclerView.Adapter<FavoritesAdapter.FavoritesHolder>() {

    fun removeItem(position: Int) {
        movieList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setClickListener(clickListener: MovieClickListener) {
        this.clickListener = clickListener
    }

    fun updateMovieList(newMovieList: List<MovieModel>) {
        movieList.clear()
        movieList.addAll(newMovieList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: ItemMovieCardBinding = DataBindingUtil.inflate(
            inflater,
            com.example.movie_project.R.layout.item_movie_card,
            parent,
            false
        )
        return FavoritesHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesHolder, position: Int) {
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

    class FavoritesHolder(var view: ItemMovieCardBinding) : RecyclerView.ViewHolder(view.root) {

    }
}

