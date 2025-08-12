package com.example.princecine.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.adapter.SupportTicketAdapter
import com.example.princecine.model.SupportTicket
import com.example.princecine.model.TicketStatus
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SupportFragment : Fragment() {
    
    private lateinit var chipAll: Chip
    private lateinit var chipPending: Chip
    private lateinit var chipResolved: Chip
    private lateinit var rvTickets: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var fabNewTicket: FloatingActionButton
    private lateinit var ticketAdapter: SupportTicketAdapter
    
    private val allTickets = mutableListOf<SupportTicket>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_support, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupRecyclerView()
        setupCapsuleClickListeners()
        setupFabClickListener()
        loadSampleData()
        updateUI()
    }
    
    private fun initializeViews(view: View) {
        chipAll = view.findViewById(R.id.chipAll)
        chipPending = view.findViewById(R.id.chipPending)
        chipResolved = view.findViewById(R.id.chipResolved)
        rvTickets = view.findViewById(R.id.rvTickets)
        llEmptyState = view.findViewById(R.id.llEmptyState)
        fabNewTicket = view.findViewById(R.id.fabNewTicket)
    }
    
    private fun setupRecyclerView() {
        ticketAdapter = SupportTicketAdapter(allTickets) { ticket ->
            // Handle ticket click
            Toast.makeText(context, "Opening ticket: ${ticket.title}", Toast.LENGTH_SHORT).show()
        }
        
        rvTickets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ticketAdapter
        }
    }
    
    private fun setupCapsuleClickListeners() {
        chipAll.setOnClickListener { selectCapsule(chipAll, null) }
        chipPending.setOnClickListener { selectCapsule(chipPending, TicketStatus.PENDING) }
        chipResolved.setOnClickListener { selectCapsule(chipResolved, TicketStatus.RESOLVED) }
    }
    
    private fun setupFabClickListener() {
        fabNewTicket.setOnClickListener {
            // TODO: Navigate to new ticket creation screen
            Toast.makeText(context, "Opening new ticket form", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun selectCapsule(selectedChip: Chip, status: TicketStatus?) {
        // Reset all capsules to unselected state
        val allChips = listOf(chipAll, chipPending, chipResolved)
        
        allChips.forEach { chip ->
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTextColor(resources.getColor(R.color.red))
        }
        
        // Set selected capsule to selected state
        selectedChip.setChipBackgroundColorResource(R.color.red)
        selectedChip.setTextColor(resources.getColor(R.color.white))
        
        // Filter tickets based on selected status
        ticketAdapter.filterTickets(status)
        updateUI()
    }
    
    private fun loadSampleData() {
        allTickets.clear()
        allTickets.addAll(listOf(
            SupportTicket(
                id = "1",
                title = "Payment Issue with Booking",
                description = "I was trying to book tickets for the new movie but the payment kept failing. The error message was unclear and I need help resolving this issue.",
                status = TicketStatus.PENDING,
                dateRaised = "Dec 15, 2024",
                ticketId = "ST001234"
            ),
            SupportTicket(
                id = "2",
                title = "Refund Request",
                description = "I need to cancel my booking and get a refund for the tickets I purchased for the movie last week.",
                status = TicketStatus.RESOLVED,
                dateRaised = "Dec 12, 2024",
                ticketId = "ST001235"
            ),
            SupportTicket(
                id = "3",
                title = "App Login Problem",
                description = "I'm unable to log into the app. It keeps showing an error message when I try to enter my credentials.",
                status = TicketStatus.PENDING,
                dateRaised = "Dec 10, 2024",
                ticketId = "ST001236"
            ),
            SupportTicket(
                id = "4",
                title = "Seat Selection Issue",
                description = "The seat selection feature is not working properly. I can't see the available seats when trying to book.",
                status = TicketStatus.RESOLVED,
                dateRaised = "Dec 8, 2024",
                ticketId = "ST001237"
            ),
            SupportTicket(
                id = "5",
                title = "Movie Schedule Query",
                description = "I want to know the schedule for the upcoming movies this weekend. The website doesn't show the complete schedule.",
                status = TicketStatus.PENDING,
                dateRaised = "Dec 5, 2024",
                ticketId = "ST001238"
            )
        ))
        
        ticketAdapter.updateTickets(allTickets)
    }
    
    private fun updateUI() {
        if (ticketAdapter.itemCount == 0) {
            rvTickets.visibility = View.GONE
            llEmptyState.visibility = View.VISIBLE
        } else {
            rvTickets.visibility = View.VISIBLE
            llEmptyState.visibility = View.GONE
        }
    }
}
