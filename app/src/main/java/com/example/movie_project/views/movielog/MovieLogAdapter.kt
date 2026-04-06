package com.example.movie_project.views.movielog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_project.R
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.databinding.ItemMovieLogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView Adapter for displaying movie log entries.
 * Each item shows the movie title and the date it was added.
 */
class MovieLogAdapter(
    private val entries: ArrayList<MovieLogEntry>,
    private var clickListener: MovieLogClickListener? = null
) : RecyclerView.Adapter<MovieLogAdapter.MovieLogViewHolder>() {

    fun setClickListener(listener: MovieLogClickListener) {
        this.clickListener = listener
    }

    fun updateEntries(newEntries: List<MovieLogEntry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemMovieLogBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_movie_log,
            parent,
            false
        )
        return MovieLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieLogViewHolder, position: Int) {
        val entry = entries[position]
        holder.bind(entry)
        holder.binding.cardMovieLogItem.setOnClickListener {
            clickListener?.onMovieLogClicked(entry)
        }
    }

    override fun getItemCount(): Int = entries.size

    /**
     * ViewHolder for a single movie log item.
     */
    class MovieLogViewHolder(val binding: ItemMovieLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        fun bind(entry: MovieLogEntry) {
            binding.entry = entry
            binding.tvMovieTitle.text = entry.movieTitle
            binding.tvDateAdded.text = "Added: ${dateFormat.format(Date(entry.dateAdded))}"
            binding.executePendingBindings()
        }
    }
}
