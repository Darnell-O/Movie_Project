package com.example.movie_project.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_project.databinding.ItemMovieCardBinding
import com.example.movie_project.models.MovieModel


class SearchAdapter(
    private var searchList: ArrayList<MovieModel>,
    private var clickListener: MovieClickListener? = null
) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    fun setClickListener(clickListener: MovieClickListener) {
        this.clickListener = clickListener
    }

    fun updateMovieList(newMovieList: List<MovieModel>) {
        searchList.clear()
        searchList.addAll(newMovieList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        val view: ItemMovieCardBinding = DataBindingUtil.inflate(
            inflater,
            com.example.movie_project.R.layout.item_movie_card,
            parent,
            false
        )
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val movie =searchList[position]
        movie?.let {
            holder.view.movie = it
    }



        holder.view.cardView.setOnClickListener {
            clickListener?.onMovieClicked(movie)

        }

    }

    override fun getItemCount(): Int {
        return searchList.size
    }

  fun filterList(filteredList: ArrayList<MovieModel>) {
        searchList = filteredList
        notifyDataSetChanged()
    }

    class SearchViewHolder(var view: ItemMovieCardBinding) : RecyclerView.ViewHolder(view.root) {

    }
}

