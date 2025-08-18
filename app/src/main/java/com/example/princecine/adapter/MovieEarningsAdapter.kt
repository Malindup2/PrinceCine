package com.example.princecine.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.model.MovieEarnings
import com.google.android.material.textview.MaterialTextView
import java.text.NumberFormat
import java.util.Locale

class MovieEarningsAdapter : ListAdapter<MovieEarnings, MovieEarningsAdapter.EarningsViewHolder>(EarningsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarningsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_earnings_card, parent, false)
        return EarningsViewHolder(view)
    }

    override fun onBindViewHolder(holder: EarningsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EarningsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivMoviePoster: ImageView = itemView.findViewById(R.id.ivMoviePoster)
        private val tvMovieTitle: MaterialTextView = itemView.findViewById(R.id.tvMovieTitle)
        private val tvTicketsSold: MaterialTextView = itemView.findViewById(R.id.tvTicketsSold)
        private val tvTotalEarnings: MaterialTextView = itemView.findViewById(R.id.tvTotalEarnings)

        fun bind(earnings: MovieEarnings) {
            // Load movie poster
            if (!earnings.posterBase64.isNullOrEmpty()) {
                try {
                    val decodedBytes = Base64.decode(earnings.posterBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    ivMoviePoster.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder)
                }
            } else if (earnings.posterResId != 0) {
                // Fallback to resource ID for backward compatibility
                ivMoviePoster.setImageResource(earnings.posterResId)
            } else {
                ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder)
            }
            
            tvMovieTitle.text = earnings.movieTitle
            
            // Format tickets sold with thousand separators
            val ticketsFormatted = NumberFormat.getNumberInstance(Locale.getDefault())
                .format(earnings.ticketsSold)
            tvTicketsSold.text = "Tickets: $ticketsFormatted"
            
            // Format earnings with currency
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "LK"))
            val earningsFormatted = currencyFormat.format(earnings.totalEarnings)
            tvTotalEarnings.text = earningsFormatted
        }
    }

    private class EarningsDiffCallback : DiffUtil.ItemCallback<MovieEarnings>() {
        override fun areItemsTheSame(oldItem: MovieEarnings, newItem: MovieEarnings): Boolean {
            return oldItem.movieId == newItem.movieId
        }

        override fun areContentsTheSame(oldItem: MovieEarnings, newItem: MovieEarnings): Boolean {
            return oldItem == newItem
        }
    }
}
