package com.example.princecine.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.example.princecine.R
import com.example.princecine.adapter.SupportTicketAdapter
import com.example.princecine.data.FirebaseRepository
import com.example.princecine.model.SupportTicket
import com.example.princecine.model.TicketStatus
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminSupportFragment : Fragment() {
    
    private lateinit var chipAll: Chip
    private lateinit var chipPending: Chip
    private lateinit var chipResolved: Chip
    private lateinit var rvTickets: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var ticketAdapter: SupportTicketAdapter
    
    private val allTickets = mutableListOf<SupportTicket>()
    private lateinit var repository: FirebaseRepository
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_support, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = FirebaseRepository()
        
        initializeViews(view)
        setupRecyclerView()
        setupCapsuleClickListeners()
        loadTicketsFromFirebase()
        updateUI()
    }
    
    private fun initializeViews(view: View) {
        chipAll = view.findViewById(R.id.chipAll)
        chipPending = view.findViewById(R.id.chipPending)
        chipResolved = view.findViewById(R.id.chipResolved)
        rvTickets = view.findViewById(R.id.rvTickets)
        llEmptyState = view.findViewById(R.id.llEmptyState)
        
        // Hide FAB for admin users
        val fabNewTicket = view.findViewById<View>(R.id.fabNewTicket)
        fabNewTicket.visibility = View.GONE
    }
    
    private fun setupRecyclerView() {
        ticketAdapter = SupportTicketAdapter(
            tickets = allTickets,
            onTicketClick = { ticket ->
                // Handle ticket click - show ticket details for admin
                showTicketDetailsDialog(ticket)
            },
            isAdmin = true,
            onSolveClick = { ticket ->
                showSolveTicketDialog(ticket)
            }
        )
        
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
    
    private fun selectCapsule(selectedChip: Chip, status: TicketStatus?) {
        // Reset all capsules to unselected state
        val allChips = listOf(chipAll, chipPending, chipResolved)
        
        allChips.forEach { chip ->
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }
        
        // Set selected capsule to selected state
        selectedChip.setChipBackgroundColorResource(R.color.red)
        selectedChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        
        // Filter tickets based on selected status
        ticketAdapter.filterTickets(status)
        updateUI()
    }
    
    private fun loadTicketsFromFirebase() {
        lifecycleScope.launch {
            try {
                val result = repository.getAllSupportTickets()
                result.onSuccess { tickets ->
                    allTickets.clear()
                    allTickets.addAll(tickets)
                    ticketAdapter.updateTickets(allTickets)
                    updateUI()
                    
                    if (tickets.isEmpty()) {
                        Toast.makeText(requireContext(), "No support tickets available.", Toast.LENGTH_SHORT).show()
                    }
                }.onFailure { error ->
                    Toast.makeText(requireContext(), "Failed to load tickets: ${error.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading tickets: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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

    private fun showTicketDetailsDialog(ticket: SupportTicket) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ticket_details, null)
        
        val tvTicketId = dialogView.findViewById<MaterialTextView>(R.id.tvTicketId)
        val tvTitle = dialogView.findViewById<MaterialTextView>(R.id.tvTitle)
        val tvDescription = dialogView.findViewById<MaterialTextView>(R.id.tvDescription)
        val tvStatus = dialogView.findViewById<MaterialTextView>(R.id.tvStatus)
        val tvDateRaised = dialogView.findViewById<MaterialTextView>(R.id.tvDateRaised)
        val btnResolve = dialogView.findViewById<MaterialButton>(R.id.btnResolve)
        val btnClose = dialogView.findViewById<MaterialButton>(R.id.btnClose)

        tvTicketId.text = "Ticket ID: ${ticket.ticketId}"
        tvTitle.text = ticket.title
        tvDescription.text = ticket.description
        tvStatus.text = "Status: ${ticket.status.name}"
        tvDateRaised.text = "Date Raised: ${ticket.dateRaised}"

        // Show/hide resolve button based on status
        if (ticket.status == TicketStatus.PENDING) {
            btnResolve.visibility = View.VISIBLE
        } else {
            btnResolve.visibility = View.GONE
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnResolve.setOnClickListener {
            dialog.dismiss()
            showSolveTicketDialog(ticket)
        }

        dialog.show()
    }

    private fun showSolveTicketDialog(ticket: SupportTicket) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_solve_ticket, null)
        
        val tvTicketTitle = dialogView.findViewById<MaterialTextView>(R.id.tvTicketTitle)
        val tvTicketDescription = dialogView.findViewById<MaterialTextView>(R.id.tvTicketDescription)
        val tilReply = dialogView.findViewById<TextInputLayout>(R.id.tilReply)
        val etReply = dialogView.findViewById<TextInputEditText>(R.id.etReply)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSubmit = dialogView.findViewById<MaterialButton>(R.id.btnSubmit)

        // Set ticket details (read-only)
        tvTicketTitle.text = ticket.title
        tvTicketDescription.text = ticket.description

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            if (validateReply(tilReply, etReply)) {
                val reply = etReply.text.toString().trim()
                resolveTicket(ticket, reply)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun validateReply(tilReply: TextInputLayout, etReply: TextInputEditText): Boolean {
        if (etReply.text.isNullOrBlank()) {
            tilReply.error = "Reply is required"
            return false
        }
        tilReply.error = null
        return true
    }

    private fun resolveTicket(ticket: SupportTicket, reply: String) {
        lifecycleScope.launch {
            try {
                val result = repository.updateTicketStatus(ticket.id, TicketStatus.RESOLVED)
                result.onSuccess {
                    // Update local data
                    ticket.status = TicketStatus.RESOLVED
                    val position = allTickets.indexOf(ticket)
                    if (position != -1) {
                        ticketAdapter.notifyItemChanged(position)
                    }
                    Toast.makeText(requireContext(), "Ticket resolved successfully!", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(requireContext(), "Failed to resolve ticket: ${error.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error resolving ticket: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

