package com.example.princecine.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.model.Movie
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class MovieAdapter(
    private val movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMoviePoster: ImageView = itemView.findViewById(R.id.ivMoviePoster)
        val tvMovieTitle: MaterialTextView = itemView.findViewById(R.id.tvMovieTitle)
        val tvRating: MaterialTextView = itemView.findViewById(R.id.tvRating)
        val btnBookNow: MaterialButton = itemView.findViewById(R.id.btnBookNow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        
        holder.ivMoviePoster.setImageResource(movie.posterResId)
        holder.tvMovieTitle.text = movie.title
        holder.tvRating.text = movie.rating
        
        holder.btnBookNow.setOnClickListener {
            onMovieClick(movie)
        }
        
        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }
    }

    override fun getItemCount(): Int = movies.size
} 