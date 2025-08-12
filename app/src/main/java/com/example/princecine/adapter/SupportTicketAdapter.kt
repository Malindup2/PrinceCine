package com.example.princecine.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.model.SupportTicket
import com.example.princecine.model.TicketStatus
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView

class SupportTicketAdapter(
    private var tickets: List<SupportTicket>,
    private val onTicketClick: (SupportTicket) -> Unit
) : RecyclerView.Adapter<SupportTicketAdapter.TicketViewHolder>() {
    
    private val allTickets = mutableListOf<SupportTicket>()

    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTicketTitle: MaterialTextView = itemView.findViewById(R.id.tvTicketTitle)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val tvDate: MaterialTextView = itemView.findViewById(R.id.tvDate)
        val tvDescription: MaterialTextView = itemView.findViewById(R.id.tvDescription)
        val tvTicketId: MaterialTextView = itemView.findViewById(R.id.tvTicketId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_support_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        
        holder.tvTicketTitle.text = ticket.title
        holder.tvDate.text = ticket.dateRaised
        holder.tvDescription.text = ticket.description
        holder.tvTicketId.text = ticket.ticketId
        
        // Set status chip
        when (ticket.status) {
            TicketStatus.PENDING -> {
                holder.chipStatus.text = "Pending"
                holder.chipStatus.setChipBackgroundColorResource(R.color.red)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.white))
            }
            TicketStatus.RESOLVED -> {
                holder.chipStatus.text = "Resolved"
                holder.chipStatus.setChipBackgroundColorResource(R.color.green)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.white))
            }
        }
        
        holder.itemView.setOnClickListener {
            onTicketClick(ticket)
        }
    }

    override fun getItemCount(): Int = tickets.size

    fun updateTickets(newTickets: List<SupportTicket>) {
        allTickets.clear()
        allTickets.addAll(newTickets)
        tickets = newTickets
        notifyDataSetChanged()
    }

    fun filterTickets(status: TicketStatus?) {
        val originalTickets = allTickets.toList()
        val filteredTickets = if (status == null) {
            originalTickets
        } else {
            originalTickets.filter { it.status == status }
        }
        updateTickets(filteredTickets)
    }
}
