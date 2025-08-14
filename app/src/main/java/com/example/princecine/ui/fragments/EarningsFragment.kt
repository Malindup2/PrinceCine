package com.example.princecine.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.adapter.MovieEarningsAdapter
import com.example.princecine.model.MovieEarnings
import com.google.android.material.textview.MaterialTextView
import java.text.NumberFormat
import java.util.Locale

class EarningsFragment : Fragment() {
    
    private lateinit var tvTotalTickets: MaterialTextView
    private lateinit var tvTotalEarnings: MaterialTextView
    private lateinit var rvMovieEarnings: RecyclerView
    private lateinit var llEmptyState: View
    private lateinit var earningsAdapter: MovieEarningsAdapter
    
    private val mockEarningsData = listOf(
        MovieEarnings(
            movieId = 1,
            movieTitle = "Atlas",
            posterResId = R.drawable.atlas,
            ticketsSold = 1245,
            totalEarnings = 1245000.0
        ),
        MovieEarnings(
            movieId = 2,
            movieTitle = "Bad Boys",
            posterResId = R.drawable.bad_boys,
            ticketsSold = 892,
            totalEarnings = 892000.0
        ),
        MovieEarnings(
            movieId = 3,
            movieTitle = "Dune: Part Two",
            posterResId = R.drawable.dube2,
            ticketsSold = 1567,
            totalEarnings = 1567000.0
        ),
        MovieEarnings(
            movieId = 4,
            movieTitle = "Fly Me to the Moon",
            posterResId = R.drawable.fly_me_to_the_moon,
            ticketsSold = 734,
            totalEarnings = 734000.0
        ),
        MovieEarnings(
            movieId = 5,
            movieTitle = "The Fall Guy",
            posterResId = R.drawable.the_fall_guy,
            ticketsSold = 1103,
            totalEarnings = 1103000.0
        )
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_earnings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupRecyclerView()
        loadEarningsData()
        updateSummaryData()
    }
    
    private fun initializeViews(view: View) {
        tvTotalTickets = view.findViewById(R.id.tvTotalTickets)
        tvTotalEarnings = view.findViewById(R.id.tvTotalEarnings)
        rvMovieEarnings = view.findViewById(R.id.rvMovieEarnings)
        llEmptyState = view.findViewById(R.id.llEmptyState)
    }
    
    private fun setupRecyclerView() {
        earningsAdapter = MovieEarningsAdapter()
        rvMovieEarnings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = earningsAdapter
        }
    }
    
    private fun loadEarningsData() {
        if (mockEarningsData.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            earningsAdapter.submitList(mockEarningsData)
        }
    }
    
    private fun updateSummaryData() {
        val totalTickets = mockEarningsData.sumOf { it.ticketsSold }
        val totalEarnings = mockEarningsData.sumOf { it.totalEarnings }
        
        // Format total tickets with thousand separators
        val ticketsFormatted = NumberFormat.getNumberInstance(Locale.getDefault())
            .format(totalTickets)
        tvTotalTickets.text = ticketsFormatted
        
        // Format total earnings with currency
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "LK"))
        val earningsFormatted = currencyFormat.format(totalEarnings)
        tvTotalEarnings.text = earningsFormatted
    }
    
    private fun showEmptyState() {
        rvMovieEarnings.visibility = View.GONE
        llEmptyState.visibility = View.VISIBLE
    }
    
    private fun hideEmptyState() {
        rvMovieEarnings.visibility = View.VISIBLE
        llEmptyState.visibility = View.GONE
    }
}
