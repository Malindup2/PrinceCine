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
import androidx.core.content.ContextCompat
import com.example.princecine.R
import com.example.princecine.adapter.SupportTicketAdapter
import com.example.princecine.model.SupportTicket
import com.example.princecine.model.TicketStatus
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*
import android.text.TextWatcher

class AdminSupportFragment : Fragment() {
    
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
            // Handle ticket click - show ticket details for admin
            showTicketDetailsDialog(ticket)
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
            showNewInquiryDialog()
        }
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

    private fun showTicketDetailsDialog(ticket: SupportTicket) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ticket_details, null)
        
        val tvTicketId = dialogView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tvTicketId)
        val tvTitle = dialogView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tvTitle)
        val tvDescription = dialogView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tvDescription)
        val tvStatus = dialogView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tvStatus)
        val tvDateRaised = dialogView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tvDateRaised)
        val btnResolve = dialogView.findViewById<MaterialButton>(R.id.btnResolve)
        val btnClose = dialogView.findViewById<MaterialButton>(R.id.btnClose)

        tvTicketId.text = "Ticket ID: ${ticket.ticketId}"
        tvTitle.text = ticket.title
        tvDescription.text = ticket.description
        tvStatus.text = "Status: ${ticket.status.name}"
        tvDateRaised.text = "Date Raised: ${ticket.dateRaised}"

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnResolve.setOnClickListener {
            if (ticket.status == TicketStatus.PENDING) {
                resolveTicket(ticket)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Ticket is already resolved", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun resolveTicket(ticket: SupportTicket) {
        ticket.status = TicketStatus.RESOLVED
        ticketAdapter.notifyDataSetChanged()
        Toast.makeText(context, "Ticket resolved successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun showNewInquiryDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_new_inquiry, null)
        
        val tilInquiryTitle = dialogView.findViewById<TextInputLayout>(R.id.tilInquiryTitle)
        val tilDescription = dialogView.findViewById<TextInputLayout>(R.id.tilDescription)
        val etInquiryTitle = dialogView.findViewById<TextInputEditText>(R.id.etInquiryTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSubmit = dialogView.findViewById<MaterialButton>(R.id.btnSubmit)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            if (validateInputs(tilInquiryTitle, tilDescription, etInquiryTitle, etDescription)) {
                submitInquiry(etInquiryTitle.text.toString(), etDescription.text.toString())
                dialog.dismiss()
            }
        }

        // Set up text change listeners to clear errors when user types
        etInquiryTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tilInquiryTitle.error = null
            }
        })

        etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tilDescription.error = null
            }
        })

        dialog.show()
    }

    private fun validateInputs(
        tilInquiryTitle: TextInputLayout,
        tilDescription: TextInputLayout,
        etInquiryTitle: TextInputEditText,
        etDescription: TextInputEditText
    ): Boolean {
        var isValid = true

        if (etInquiryTitle.text.isNullOrBlank()) {
            tilInquiryTitle.error = "Inquiry title is required"
            isValid = false
        }

        if (etDescription.text.isNullOrBlank()) {
            tilDescription.error = "Description is required"
            isValid = false
        }

        return isValid
    }

    private fun submitInquiry(title: String, description: String) {
        try {
            val newTicketId = generateTicketId()
            
            val newTicket = SupportTicket(
                id = (allTickets.size + 1).toString(),
                title = title,
                description = description,
                status = TicketStatus.PENDING,
                dateRaised = getCurrentDate(),
                ticketId = newTicketId
            )

            allTickets.add(0, newTicket)
            ticketAdapter.updateTickets(allTickets)
            updateUI()
            
            Toast.makeText(context, "Inquiry submitted successfully!", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(context, "Error submitting inquiry: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun generateTicketId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(1000)
        return "ST${timestamp}${random}"
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

