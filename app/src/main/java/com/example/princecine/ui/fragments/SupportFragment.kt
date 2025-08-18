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
import com.example.princecine.service.AuthService
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
import android.text.Editable
import kotlinx.coroutines.launch

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
        ticketAdapter = SupportTicketAdapter(
            tickets = allTickets,
            onTicketClick = { ticket ->
                // Handle ticket click
                Toast.makeText(context, "Opening ticket: ${ticket.title}", Toast.LENGTH_SHORT).show()
            },
            isAdmin = false,
            onSolveClick = null
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
                dateRaised = com.google.firebase.Timestamp.now(),
                ticketId = "ST001234"
            ),
            SupportTicket(
                id = "2",
                title = "Refund Request",
                description = "I need to cancel my booking and get a refund for the tickets I purchased for the movie last week.",
                status = TicketStatus.RESOLVED,
                dateRaised = com.google.firebase.Timestamp.now(),
                ticketId = "ST001235"
            ),
            SupportTicket(
                id = "3",
                title = "App Login Problem",
                description = "I'm unable to log into the app. It keeps showing an error message when I try to enter my credentials.",
                status = TicketStatus.PENDING,
                dateRaised = com.google.firebase.Timestamp.now(),
                ticketId = "ST001236"
            ),
            SupportTicket(
                id = "4",
                title = "Seat Selection Issue",
                description = "The seat selection feature is not working properly. I can't see the available seats when trying to book.",
                status = TicketStatus.RESOLVED,
                dateRaised = com.google.firebase.Timestamp.now(),
                ticketId = "ST001237"
            ),
            SupportTicket(
                id = "5",
                title = "Movie Schedule Query",
                description = "I want to know the schedule for the upcoming movies this weekend. The website doesn't show the complete schedule.",
                status = TicketStatus.PENDING,
                dateRaised = com.google.firebase.Timestamp.now(),
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

        // Set up button click listeners
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
        etInquiryTitle.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tilInquiryTitle.error = null
            }
        })

        etDescription.addTextChangedListener(object : android.text.TextWatcher {
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

        // Validate inquiry title
        if (etInquiryTitle.text.isNullOrBlank()) {
            tilInquiryTitle.error = "Inquiry title is required"
            isValid = false
        }

        // Validate description
        if (etDescription.text.isNullOrBlank()) {
            tilDescription.error = "Description is required"
            isValid = false
        }

        return isValid
    }

    private fun submitInquiry(title: String, description: String) {
        try {
            // Generate new ticket ID
            val newTicketId = generateTicketId()
            
            // Create new support ticket
            val newTicket = SupportTicket(
                id = (allTickets.size + 1).toString(),
                title = title,
                description = description,
                status = TicketStatus.PENDING,
                dateRaised = com.google.firebase.Timestamp.now(),
                ticketId = newTicketId
            )

            // Add to the beginning of the list
            allTickets.add(0, newTicket)
            
            // Update adapter
            ticketAdapter.updateTickets(allTickets)
            
            // Update UI
            updateUI()
            
            // Show success message
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
