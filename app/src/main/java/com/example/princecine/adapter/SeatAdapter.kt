package com.example.princecine.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                holder.btnSeat.setBackgroundColor(holder.itemView.context.getColor(R.color.grey))
                holder.btnSeat.setTextColor(holder.itemView.context.getColor(R.color.black))
                holder.btnSeat.isEnabled = false
            }
            seat.isSelected -> {
                holder.btnSeat.setBackgroundColor(holder.itemView.context.getColor(R.color.red))
                holder.btnSeat.setTextColor(holder.itemView.context.getColor(R.color.white))
                holder.btnSeat.isEnabled = true
            }
            else -> {
                holder.btnSeat.setBackgroundColor(holder.itemView.context.getColor(R.color.white))
                holder.btnSeat.setTextColor(holder.itemView.context.getColor(R.color.black))
                holder.btnSeat.isEnabled = true
            }
        }
        
        holder.btnSeat.setOnClickListener {
            onSeatClick(seat)
        }
    }

    override fun getItemCount(): Int = seats.size
    
    fun getSeats(): List<Seat> = seats
    
    fun updateSeats(newSeats: List<Seat>) {
        seats = newSeats
        notifyDataSetChanged()
    }
}
