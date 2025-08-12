package com.example.princecine.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.adapter.SeatAdapter
import com.example.princecine.model.Seat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class SeatSelectionActivity : AppCompatActivity() {
    
    companion object {
        private const val EXTRA_MOVIE_TITLE = "extra_movie_title"
        private const val EXTRA_MOVIE_DATE = "extra_movie_date"
        private const val EXTRA_MOVIE_TIME = "extra_movie_time"
        private const val SEAT_PRICE = 12.99
        
        fun newIntent(context: Context, movieTitle: String, date: String, time: String): Intent {
            return Intent(context, SeatSelectionActivity::class.java).apply {
                putExtra(EXTRA_MOVIE_TITLE, movieTitle)
                putExtra(EXTRA_MOVIE_DATE, date)
                putExtra(EXTRA_MOVIE_TIME, time)
            }
        }
    }
    
    private lateinit var ivBackButton: ImageView
    private lateinit var tvMovieTitle: MaterialTextView
    private lateinit var rvSeats: RecyclerView
    private lateinit var tvTotalPrice: MaterialTextView
    private lateinit var tvSelectedSeats: MaterialTextView
    private lateinit var btnContinue: MaterialButton
    
    private lateinit var seatAdapter: SeatAdapter
    private val selectedSeats = mutableSetOf<Seat>()
    private var totalPrice = 0.0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_selection)
        
        initializeViews()
        setupClickListeners()
        loadMovieData()
        setupSeatGrid()
        updateBookingSummary()
    }
    
    private fun initializeViews() {
        ivBackButton = findViewById(R.id.ivBackButton)
        tvMovieTitle = findViewById(R.id.tvMovieTitle)
        rvSeats = findViewById(R.id.rvSeats)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        tvSelectedSeats = findViewById(R.id.tvSelectedSeats)
        btnContinue = findViewById(R.id.btnContinue)
    }
    
    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            finish()
        }
        
        btnContinue.setOnClickListener {
            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Please select at least one seat", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Navigate to payment/confirmation screen
                val seatNumbers = selectedSeats.joinToString(", ") { it.seatNumber }
                Toast.makeText(this, "Booking $seatNumbers for $${String.format("%.2f", totalPrice)}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun loadMovieData() {
        val movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE) ?: "Unknown Movie"
        val date = intent.getStringExtra(EXTRA_MOVIE_DATE) ?: "Today"
        val time = intent.getStringExtra(EXTRA_MOVIE_TIME) ?: "12:30 PM"
        
        tvMovieTitle.text = "$movieTitle - $date, $time"
    }
    
    private fun setupSeatGrid() {
        val seats = generateSeatLayout()
        seatAdapter = SeatAdapter(seats) { seat ->
            toggleSeatSelection(seat)
        }
        
        rvSeats.layoutManager = GridLayoutManager(this, 10) // 10 columns for seat numbers 1-10
        rvSeats.adapter = seatAdapter
    }
    
    private fun generateSeatLayout(): List<Seat> {
        val seats = mutableListOf<Seat>()
        val rows = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")
        
        // Pre-defined taken seats based on the image
        val takenSeats = setOf(
            "A3", "A4", "A5", "B2", "B3", "B6", "B7", "B8", "B9", "B10",
            "C2", "C3", "C6", "C7", "C8", "C9", "C10", "D3", "D6", "D7", "D8", "D9", "D10",
            "E3", "E6", "E7", "E8", "E9", "E10", "F3", "F5", "F6", "F7", "F8", "F9", "F10",
            "G3", "G4", "G5", "G8", "H2", "H3", "H4", "H7", "H8", "I3", "I8", "J3", "J4", "J7", "J8", "K3", "K4", "K7", "K8"
        )
        
        rows.forEach { row ->
            for (col in 1..10) {
                val seatNumber = "$row$col"
                val isTaken = takenSeats.contains(seatNumber)
                val seat = Seat(
                    id = seatNumber,
                    seatNumber = seatNumber,
                    row = row,
                    column = col,
                    isTaken = isTaken,
                    isSelected = false
                )
                seats.add(seat)
            }
        }
        
        return seats
    }
    
    private fun toggleSeatSelection(seat: Seat) {
        if (seat.isTaken) {
            Toast.makeText(this, "This seat is already taken", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedSeats.contains(seat)) {
            // Deselect seat
            selectedSeats.remove(seat)
            seat.isSelected = false
            totalPrice -= SEAT_PRICE
        } else {
            // Select seat
            selectedSeats.add(seat)
            seat.isSelected = true
            totalPrice += SEAT_PRICE
        }
        
        seatAdapter.notifyItemChanged(seatAdapter.getSeats().indexOf(seat))
        updateBookingSummary()
    }
    
    private fun updateBookingSummary() {
        tvTotalPrice.text = "$${String.format("%.2f", totalPrice)}"
        tvSelectedSeats.text = if (selectedSeats.isEmpty()) {
            ""
        } else {
            selectedSeats.joinToString(", ") { it.seatNumber }
        }
        
        // Update continue button state
        btnContinue.isEnabled = selectedSeats.isNotEmpty()
        btnContinue.alpha = if (selectedSeats.isNotEmpty()) 1.0f else 0.5f
    }
}
