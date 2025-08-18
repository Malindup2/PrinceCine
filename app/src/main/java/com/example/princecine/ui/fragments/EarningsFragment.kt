package com.example.princecine.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.adapter.MovieEarningsAdapter
import com.example.princecine.data.FirebaseRepository
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
    
    private lateinit var repository: FirebaseRepository
    private var earningsList = mutableListOf<MovieEarnings>()
    private var unsubscribeEarningsListener: (() -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_earnings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = FirebaseRepository()
        initializeViews(view)
        setupRecyclerView()
        setupRealtimeEarningsListener()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Unsubscribe from real-time listener
        unsubscribeEarningsListener?.invoke()
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
    
    private fun setupRealtimeEarningsListener() {
        unsubscribeEarningsListener = repository.getMovieEarningsRealtime { earnings ->
            earningsList.clear()
            earningsList.addAll(earnings)
            
            showLoading(false)
            
            if (earnings.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                earningsAdapter.submitList(earnings)
                updateSummaryData(earnings)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        // Simple loading state
        rvMovieEarnings.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun updateSummaryData(earnings: List<MovieEarnings>) {
        val totalTickets = earnings.sumOf { it.ticketsSold }
        val totalEarnings = earnings.sumOf { it.totalEarnings }
        
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
