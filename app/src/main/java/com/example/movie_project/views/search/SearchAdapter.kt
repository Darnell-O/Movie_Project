package com.example.movie_project.views.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.movie_project.R
import com.example.movie_project.databinding.ItemMovieCardBinding
import com.example.movie_project.models.MovieModel
import com.example.movie_project.util.HapticUtil
import com.example.movie_project.views.MovieClickListener

class SearchAdapter(
    private var clickListener: MovieClickListener? = null
) : ListAdapter<MovieModel, SearchAdapter.SearchViewHolder>(MovieDiffCallback()) {

    fun setClickListener(clickListener: MovieClickListener) {
        this.clickListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: ItemMovieCardBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_movie_card,
            parent,
            false
        )
        val holder = SearchViewHolder(view)

        view.cardView.setOnClickListener { cardView ->
            HapticUtil.performClickFeedback(cardView)
            val position = holder.adapterPosition
            if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                clickListener?.onMovieClicked(getItem(position))
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.view.movie = getItem(position)
    }

    class SearchViewHolder(var view: ItemMovieCardBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(view.root)

    class MovieDiffCallback : DiffUtil.ItemCallback<MovieModel>() {
        override fun areItemsTheSame(oldItem: MovieModel, newItem: MovieModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MovieModel, newItem: MovieModel): Boolean {
            return oldItem == newItem
        }
    }
}
