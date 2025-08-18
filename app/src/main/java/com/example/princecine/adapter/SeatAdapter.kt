package com.example.princecine.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.model.Seat
import com.google.android.material.button.MaterialButton

class SeatAdapter(
    private var seats: List<Seat>,
    private val onSeatClick: (Seat) -> Unit
) : RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {

    class SeatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnSeat: MaterialButton = itemView.findViewById(R.id.btnSeat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seat, parent, false)
        return SeatViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val seat = seats[position]
        
        holder.btnSeat.text = seat.seatNumber
        
        // Set seat appearance based on status
        when {
            seat.isTaken -> {
                // Taken seats - grey color, disabled
                holder.btnSeat.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.grey))
                holder.btnSeat.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
                holder.btnSeat.isEnabled = false
                holder.btnSeat.alpha = 0.6f
            }
            seat.isSelected -> {
                // Selected seats - red color, active
                holder.btnSeat.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
                holder.btnSeat.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
                holder.btnSeat.isEnabled = true
                holder.btnSeat.alpha = 1.0f
                // Add slight elevation for selected seats
                holder.btnSeat.elevation = 8f
            }
            else -> {
                // Available seats - white background with border
                holder.btnSeat.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
                holder.btnSeat.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                holder.btnSeat.isEnabled = true
                holder.btnSeat.alpha = 1.0f
                holder.btnSeat.elevation = 4f
                // Add border to show it's clickable
                holder.btnSeat.strokeColor = ContextCompat.getColorStateList(holder.itemView.context, R.color.red)
                holder.btnSeat.strokeWidth = 2
            }
        }
        
        holder.btnSeat.setOnClickListener {
            if (seat.isEnabled()) {
                onSeatClick(seat)
            }
        }
    }

    override fun getItemCount(): Int = seats.size
    
    fun getSeats(): List<Seat> = seats
    
    fun updateSeats(newSeats: List<Seat>) {
        seats = newSeats
        notifyDataSetChanged()
    }
}
