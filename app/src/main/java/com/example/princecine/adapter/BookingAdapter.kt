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
import com.example.princecine.model.Booking
import com.example.princecine.model.BookingStatus
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*

class BookingAdapter(
    private val bookings: List<Booking>,
    private val onBookingClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMoviePoster: ImageView = itemView.findViewById(R.id.ivMoviePoster)
        val tvMovieTitle: MaterialTextView = itemView.findViewById(R.id.tvMovieTitle)
        val tvRating: MaterialTextView = itemView.findViewById(R.id.tvRating) // Repurpose for booking info
        val btnBookNow: MaterialButton = itemView.findViewById(R.id.btnBookNow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        Log.d("BookingAdapter", "Binding booking: ${booking.movieTitle}")

        holder.tvMovieTitle.text = booking.movieTitle
        
        // Combine all booking info in the rating TextView
        val bookingInfo = """
            ${booking.showDate} at ${booking.showTime}
            Seats: ${booking.seats.joinToString(", ")}
            Amount: $${booking.totalAmount}
            Status: ${booking.status.name}
            ID: ${booking.bookingId}
        """.trimIndent()
        holder.tvRating.text = bookingInfo

        // Load movie poster if available
        if (booking.moviePosterUrl.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(booking.moviePosterUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.ivMoviePoster.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder)
            }
        } else {
            holder.ivMoviePoster.setImageResource(R.drawable.ic_movie_placeholder)
        }

        holder.btnBookNow.text = "View Details"
        holder.btnBookNow.setOnClickListener {
            onBookingClick(booking)
        }

        holder.itemView.setOnClickListener {
            onBookingClick(booking)
        }
    }

    override fun getItemCount(): Int {
        Log.d("BookingAdapter", "getItemCount: ${bookings.size}")
        return bookings.size
    }
}
