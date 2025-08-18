package com.example.princecine.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.model.Movie
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class AdminMovieAdapter(
    private val movies: List<Movie>,
    private val onEditClick: (Movie) -> Unit,
    private val onDeleteClick: (Movie) -> Unit
) : RecyclerView.Adapter<AdminMovieAdapter.AdminMovieViewHolder>() {

    class AdminMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMoviePoster: ImageView = itemView.findViewById(R.id.ivMoviePoster)
        val tvMovieTitle: MaterialTextView = itemView.findViewById(R.id.tvMovieTitle)
        val tvRating: MaterialTextView = itemView.findViewById(R.id.tvRating)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminMovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_movie_card, parent, false)
        return AdminMovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminMovieViewHolder, position: Int) {
        val movie = movies[position]
        Log.d("AdminMovieAdapter", "Binding movie at position $position: ${movie.title}")
        
        // Load movie poster
        if (!movie.posterBase64.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(movie.posterBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.ivMoviePoster.setImageBitmap(bitmap)
                Log.d("AdminMovieAdapter", "Loaded Base64 image for ${movie.title}")
            } catch (e: Exception) {
                Log.e("AdminMovieAdapter", "Failed to load Base64 image for ${movie.title}", e)
                holder.ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder)
            }
        } else if (movie.posterResId != 0) {
            // Fallback to resource ID for backward compatibility
            holder.ivMoviePoster.setImageResource(movie.posterResId)
            Log.d("AdminMovieAdapter", "Loaded resource image for ${movie.title}")
        } else {
            holder.ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder)
            Log.d("AdminMovieAdapter", "Using placeholder image for ${movie.title}")
        }
        
        holder.tvMovieTitle.text = movie.title
        holder.tvRating.text = "${movie.rating}/5"
        
        holder.btnEdit.setOnClickListener {
            onEditClick(movie)
        }
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(movie)
        }
    }

    override fun getItemCount(): Int {
        Log.d("AdminMovieAdapter", "getItemCount called, returning ${movies.size}")
        return movies.size
    }
}