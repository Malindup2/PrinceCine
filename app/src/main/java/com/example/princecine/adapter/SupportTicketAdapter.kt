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
import com.google.android.material.button.MaterialButton

class SupportTicketAdapter(
    private var tickets: List<SupportTicket>,
    private val onTicketClick: (SupportTicket) -> Unit,
    private val isAdmin: Boolean = false,
    private val onSolveClick: ((SupportTicket) -> Unit)? = null
) : RecyclerView.Adapter<SupportTicketAdapter.TicketViewHolder>() {
    
    private val allTickets = mutableListOf<SupportTicket>()

    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTicketTitle: MaterialTextView = itemView.findViewById(R.id.tvTicketTitle)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val tvDate: MaterialTextView = itemView.findViewById(R.id.tvDate)
        val tvDescription: MaterialTextView = itemView.findViewById(R.id.tvDescription)
        val tvTicketId: MaterialTextView = itemView.findViewById(R.id.tvTicketId)
        val btnSolve: MaterialButton? = itemView.findViewById(R.id.btnSolve)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_support_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        
        holder.tvTicketTitle.text = ticket.title
        holder.tvDate.text = ticket.dateRaised?.toDate()?.toString() ?: "Unknown Date"
        holder.tvDescription.text = ticket.description
        holder.tvTicketId.text = ticket.ticketId
        
        // Set status chip
        when (ticket.status) {
            TicketStatus.PENDING -> {
                holder.chipStatus.text = "Pending"
                holder.chipStatus.setChipBackgroundColorResource(R.color.red)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.white))
            }
            TicketStatus.IN_PROGRESS -> {
                holder.chipStatus.text = "In Progress"
                holder.chipStatus.setChipBackgroundColorResource(R.color.accent)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.white))
            }
            TicketStatus.RESOLVED -> {
                holder.chipStatus.text = "Solved"
                holder.chipStatus.setChipBackgroundColorResource(R.color.green)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.white))
            }
            TicketStatus.CLOSED -> {
                holder.chipStatus.text = "Closed"
                holder.chipStatus.setChipBackgroundColorResource(R.color.grey)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.white))
            }
        }
        
        // Handle solve button for admin users
        if (isAdmin && holder.btnSolve != null) {
            if (ticket.status == TicketStatus.PENDING) {
                holder.btnSolve.visibility = View.VISIBLE
                holder.btnSolve.setOnClickListener {
                    onSolveClick?.invoke(ticket)
                }
            } else {
                holder.btnSolve.visibility = View.GONE
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
