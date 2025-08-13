package com.example.princecine.adapter

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
        
        holder.ivMoviePoster.setImageResource(movie.posterResId)
        holder.tvMovieTitle.text = movie.title
        holder.tvRating.text = movie.rating
        
        holder.btnEdit.setOnClickListener {
            onEditClick(movie)
        }
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(movie)
        }
    }

    override fun getItemCount(): Int = movies.size
}

