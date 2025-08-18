package com.example.princecine.ui.fragments

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.adapter.AdminMovieAdapter
import com.example.princecine.model.Movie
import com.example.princecine.data.MovieDataManager
import com.example.princecine.data.FirebaseRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AdminHomeFragment : Fragment() {
    
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
    private var movies = mutableListOf<Movie>()
    private lateinit var movieAdapter: AdminMovieAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
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
                // TODO: Implement search functionality
                val searchQuery = s.toString().trim()
                // Filter movies based on search query
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
        
        // TODO: Implement filter functionality based on selected capsule
        val selectedGenre = selectedChip.text.toString()
        // Filter movies based on selected genre
    }
    
    private fun setupRecyclerView() {
        movieAdapter = AdminMovieAdapter(
            movies = movies,
            onEditClick = { movie ->
                showEditMovieDialog(movie)
            },
            onDeleteClick = { movie ->
                showDeleteConfirmationDialog(movie)
            }
        )
        
        rvMovies.layoutManager = GridLayoutManager(context, 2)
        rvMovies.adapter = movieAdapter
    }
    
    private fun setupRealtimeMovieListener() {
        Log.d("AdminHomeFragment", "Setting up real-time movie listener")
        unsubscribeMovieListener = repository.getMoviesRealtime { movieList ->
            Log.d("AdminHomeFragment", "Received ${movieList.size} movies from listener")
            movieList.forEachIndexed { index, movie ->
                Log.d("AdminHomeFragment", "Movie $index: ${movie.title} (ID: ${movie.id})")
            }
            
            movies.clear()
            movies.addAll(movieList)
            Log.d("AdminHomeFragment", "Updated movies list, now has ${movies.size} movies")
            
            showLoading(false)
            
            if (movieList.isEmpty()) {
                Log.d("AdminHomeFragment", "No movies found, showing empty state")
                showEmptyState(true)
            } else {
                Log.d("AdminHomeFragment", "Movies found, notifying adapter")
                showEmptyState(false)
                movieAdapter.notifyDataSetChanged()
                Log.d("AdminHomeFragment", "Adapter notified of data change")
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
            Toast.makeText(requireContext(), "No movies found. Add movies using the + button.", Toast.LENGTH_LONG).show()
        }
    }
    
    fun refreshMovieList() {
        // Real-time listener will automatically refresh
        // This method kept for compatibility
    }
    
    private fun showEditMovieDialog(movie: Movie) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_movie, null)
        
        // Initialize views
        val ivMoviePoster: ImageView = dialogView.findViewById(R.id.ivMoviePoster)
        val btnChangeImage: MaterialButton = dialogView.findViewById(R.id.btnChangeImage)
        val etMovieName: TextInputEditText = dialogView.findViewById(R.id.etMovieName)
        val etDescription: TextInputEditText = dialogView.findViewById(R.id.etDescription)
        val ratingBar: RatingBar = dialogView.findViewById(R.id.ratingBar)
        val tvRatingValue: MaterialTextView = dialogView.findViewById(R.id.tvRatingValue)
        val spinnerGenre: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerGenre)
        val etMovieTimes: TextInputEditText = dialogView.findViewById(R.id.etMovieTimes)
        val btnCancel: MaterialButton = dialogView.findViewById(R.id.btnCancel)
        val btnSave: MaterialButton = dialogView.findViewById(R.id.btnSave)
        
        // Set current values
        ivMoviePoster.setImageResource(movie.posterResId)
        etMovieName.setText(movie.title)
        etDescription.setText(movie.description)
        
        // Set rating
        val rating = movie.rating.toFloat()
        val ratingOutOf5 = (rating / 2.0f).coerceIn(0f, 5f)
        ratingBar.rating = ratingOutOf5
        tvRatingValue.text = "${ratingOutOf5}/5"
        
        // Update rating display when rating bar changes
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            tvRatingValue.text = "${rating}/5"
        }
        
        // Set genre
        etMovieTimes.setText(movie.movieTimes)
        
        // Setup genre spinner
        val genres = arrayOf("Action", "Sci-Fi", "Drama", "Horror", "Thriller", "Comedy", "Romance", "Adventure")
        val genreAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genres)
        spinnerGenre.setAdapter(genreAdapter)
        spinnerGenre.setText(movie.genre, false)
        
        // Create dialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        // Setup button click listeners
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnSave.setOnClickListener {
            if (validateInputs(etMovieName, etDescription, etMovieTimes)) {
                val updatedMovie = movie.copy(
                    title = etMovieName.text.toString().trim(),
                    description = etDescription.text.toString().trim(),
                    rating = (ratingBar.rating * 2).toDouble(),
                    genre = spinnerGenre.text.toString(),
                    movieTimes = etMovieTimes.text.toString().trim()
                )
                
                updateMovie(updatedMovie)
                dialog.dismiss()
                Toast.makeText(context, "Movie updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnChangeImage.setOnClickListener {
            // TODO: Implement image picker functionality
            Toast.makeText(context, "Image picker functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        dialog.show()
    }
    
    private fun validateInputs(
        etMovieName: TextInputEditText,
        etDescription: TextInputEditText,
        etMovieTimes: TextInputEditText
    ): Boolean {
        var isValid = true
        
        // Validate movie name
        if (etMovieName.text.toString().trim().isEmpty()) {
            etMovieName.error = "Movie name is required"
            isValid = false
        } else {
            etMovieName.error = null
        }
        
        // Validate description
        if (etDescription.text.toString().trim().isEmpty()) {
            etDescription.error = "Description is required"
            isValid = false
        } else {
            etDescription.error = null
        }
        
        // Validate movie times
        if (etMovieTimes.text.toString().trim().isEmpty()) {
            etMovieTimes.error = "Movie times are required"
            isValid = false
        } else {
            etMovieTimes.error = null
        }
        
        return isValid
    }
    
    private fun updateMovie(updatedMovie: Movie) {
        coroutineScope.launch {
            try {
                val result = repository.updateMovie(updatedMovie)
                result.onSuccess {
                    Toast.makeText(context, "Movie updated successfully", Toast.LENGTH_SHORT).show()
                    // Real-time listener will automatically update the UI
                }.onFailure { error ->
                    Toast.makeText(context, "Failed to update movie: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error updating movie: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDeleteConfirmationDialog(movie: Movie) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Movie")
            .setMessage("Are you sure you want to delete '${movie.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteMovie(movie)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteMovie(movie: Movie) {
        coroutineScope.launch {
            try {
                val result = repository.deleteMovie(movie.id)
                result.onSuccess {
                    Toast.makeText(context, "Movie deleted successfully", Toast.LENGTH_SHORT).show()
                    // Real-time listener will automatically update the UI
                }.onFailure { error ->
                    Toast.makeText(context, "Failed to delete movie: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error deleting movie: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

