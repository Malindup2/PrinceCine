package com.example.princecine.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.adapter.MovieAdapter
import com.example.princecine.data.FirebaseRepository
import com.example.princecine.model.Movie
import com.example.princecine.ui.MovieDetailsActivity
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    
    private lateinit var etSearch: TextInputEditText
    private lateinit var chipAll: Chip
    private lateinit var chipSciFi: Chip
    private lateinit var chipAction: Chip
    private lateinit var chipDrama: Chip
    private lateinit var chipHorror: Chip
    private lateinit var chipThriller: Chip
    private lateinit var chipComedy: Chip
    private lateinit var rvMovies: RecyclerView
    
    private lateinit var repository: FirebaseRepository
    private var allMovies = mutableListOf<Movie>()
    private lateinit var movieAdapter: MovieAdapter
    private var unsubscribeMovieListener: (() -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = FirebaseRepository()
        
        initializeViews(view)
        setupSearchListener()
        setupCapsuleClickListeners()
        setupRecyclerView()
        setupRealtimeMovieListener()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Unsubscribe from real-time listener
        unsubscribeMovieListener?.invoke()
    }
    
    private fun initializeViews(view: View) {
        etSearch = view.findViewById(R.id.etSearch)
        chipAll = view.findViewById(R.id.chipAll)
        chipSciFi = view.findViewById(R.id.chipSciFi)
        chipAction = view.findViewById(R.id.chipAction)
        chipDrama = view.findViewById(R.id.chipDrama)
        chipHorror = view.findViewById(R.id.chipHorror)
        chipThriller = view.findViewById(R.id.chipThriller)
        chipComedy = view.findViewById(R.id.chipComedy)
        rvMovies = view.findViewById(R.id.rvMovies)
    }
    
    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchQuery = s.toString().trim()
                val selectedGenre = getSelectedGenre()
                filterMovies(searchQuery, selectedGenre)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun setupCapsuleClickListeners() {
        chipAll.setOnClickListener { selectCapsule(chipAll) }
        chipSciFi.setOnClickListener { selectCapsule(chipSciFi) }
        chipAction.setOnClickListener { selectCapsule(chipAction) }
        chipDrama.setOnClickListener { selectCapsule(chipDrama) }
        chipHorror.setOnClickListener { selectCapsule(chipHorror) }
        chipThriller.setOnClickListener { selectCapsule(chipThriller) }
        chipComedy.setOnClickListener { selectCapsule(chipComedy) }
    }
    
    private fun selectCapsule(selectedChip: Chip) {
        // Reset all capsules to unselected state
        val allChips = listOf(chipAll, chipSciFi, chipAction, chipDrama, chipHorror, chipThriller, chipComedy)
        
        allChips.forEach { chip ->
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTextColor(resources.getColor(R.color.red))
        }
        
        // Set selected capsule to selected state
        selectedChip.setChipBackgroundColorResource(R.color.red)
        selectedChip.setTextColor(resources.getColor(R.color.white))
        
        // Filter movies based on selected genre
        val selectedGenre = selectedChip.text.toString()
        val searchQuery = etSearch.text.toString().trim()
        filterMovies(searchQuery, selectedGenre)
    }
    
    private fun getSelectedGenre(): String {
        val allChips = listOf(chipAll, chipSciFi, chipAction, chipDrama, chipHorror, chipThriller, chipComedy)
        return allChips.find { chip ->
            chip.chipBackgroundColor != null // This is a simple check, you might need to adjust based on your implementation
        }?.text?.toString() ?: "All"
    }
    
    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(allMovies) { movie ->
            // Navigate to movie details
            val intent = MovieDetailsActivity.newIntent(requireContext(), movie)
            startActivity(intent)
        }
        
        rvMovies.layoutManager = GridLayoutManager(context, 2)
        rvMovies.adapter = movieAdapter
    }
    
    private fun setupRealtimeMovieListener() {
        Log.d("HomeFragment", "Setting up real-time movie listener")
        unsubscribeMovieListener = repository.getMoviesRealtime { movies ->
            Log.d("HomeFragment", "Received ${movies.size} movies from listener")
            movies.forEachIndexed { index, movie ->
                Log.d("HomeFragment", "Movie $index: ${movie.title} (${movie.id})")
            }
            
            allMovies.clear()
            allMovies.addAll(movies)
            
            // Apply current filters
            val searchQuery = etSearch.text.toString().trim()
            val selectedGenre = getSelectedGenre()
            
            showLoading(false)
            
            if (movies.isEmpty()) {
                Log.d("HomeFragment", "No movies found, showing empty state")
                showEmptyState(true)
            } else {
                Log.d("HomeFragment", "Movies found, filtering with query='$searchQuery' genre='$selectedGenre'")
                showEmptyState(false)
                filterMovies(searchQuery, selectedGenre)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        // Simple loading state - just show/hide recycler view
        rvMovies.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showEmptyState(show: Boolean) {
        // Simple empty state handling
        if (show) {
            Toast.makeText(requireContext(), "No movies available. Admin can add movies.", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun filterMovies(query: String = "", genre: String = "All") {
        val filteredMovies = allMovies.filter { movie ->
            val matchesQuery = query.isEmpty() || movie.title.contains(query, ignoreCase = true)
            val matchesGenre = genre == "All" || movie.genre.equals(genre, ignoreCase = true)
            matchesQuery && matchesGenre
        }
        
        movieAdapter.updateMovies(filteredMovies)
    }
} 