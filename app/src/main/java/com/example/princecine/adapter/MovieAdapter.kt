package com.example.princecine.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
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
    private var movies: MutableList<Movie>,
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
        
        // Load movie poster
        if (!movie.posterBase64.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(movie.posterBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.ivMoviePoster.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder)
            }
        } else if (movie.posterResId != 0) {
            // Fallback to resource ID for backward compatibility
            holder.ivMoviePoster.setImageResource(movie.posterResId)
        } else {
            holder.ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder)
        }
        
        holder.tvMovieTitle.text = movie.title
        holder.tvRating.text = "${movie.rating}/5"
        
        holder.btnBookNow.setOnClickListener {
            onMovieClick(movie)
        }
        
        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }
    }

    override fun getItemCount(): Int = movies.size
    
    fun updateMovies(newMovies: List<Movie>) {
        Log.d("MovieAdapter", "Updating movies: ${newMovies.size} movies")
        newMovies.forEachIndexed { index, movie ->
            Log.d("MovieAdapter", "Movie $index: ${movie.title}")
        }
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
        Log.d("MovieAdapter", "Movies updated and adapter notified")
    }
} 