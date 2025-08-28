package com.example.princecine.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import java.io.IOException

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
    private var allMovies = mutableListOf<Movie>() // Store all movies for filtering
    private lateinit var movieAdapter: AdminMovieAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var unsubscribeMovieListener: (() -> Unit)? = null
    
    private var currentSearchQuery = ""
    private var currentSelectedGenre = "All"
    
    // Image picker for edit dialog
    private var editDialogImageView: ImageView? = null
    private var selectedImageBitmap: Bitmap? = null
    private var currentEditingMovie: Movie? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                // Use smaller size to ensure it fits in Firestore - max 400x600 pixels
                val resizedBitmap = resizeBitmap(bitmap, 400, 600)
                selectedImageBitmap = resizedBitmap
                editDialogImageView?.setImageBitmap(resizedBitmap)
                
                // Log the size for debugging
                val base64Size = bitmapToBase64(resizedBitmap).length
                Log.d("AdminHomeFragment", "Base64 image size will be: $base64Size characters")
            } catch (e: IOException) {
                Log.e("AdminHomeFragment", "Error loading image", e)
                Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
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
                currentSearchQuery = s.toString().trim()
                filterMovies()
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
        
        // Update current selected genre and filter
        currentSelectedGenre = selectedChip.text.toString()
        filterMovies()
    }
    
    private fun filterMovies() {
        Log.d("AdminHomeFragment", "filterMovies called with allMovies.size=${allMovies.size}, search='$currentSearchQuery', genre='$currentSelectedGenre'")
        
        val filteredMovies = allMovies.filter { movie ->
            val matchesSearch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                movie.title.contains(currentSearchQuery, ignoreCase = true) ||
                movie.description.contains(currentSearchQuery, ignoreCase = true) ||
                movie.genre.contains(currentSearchQuery, ignoreCase = true)
            }
            
            val matchesGenre = if (currentSelectedGenre == "All") {
                true
            } else {
                movie.genre.equals(currentSelectedGenre, ignoreCase = true)
            }
            
            val matches = matchesSearch && matchesGenre
            Log.d("AdminHomeFragment", "Movie '${movie.title}': search=$matchesSearch, genre=$matchesGenre, final=$matches")
            matches
        }
        
        movies.clear()
        movies.addAll(filteredMovies)
        movieAdapter.notifyDataSetChanged()
        
        Log.d("AdminHomeFragment", "Filtered movies: ${movies.size} of ${allMovies.size}")
        Log.d("AdminHomeFragment", "RecyclerView visibility: ${rvMovies.visibility}")
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
        
        // First, update any existing movies that don't have the isActive field
        coroutineScope.launch {
            try {
                repository.updateExistingMoviesWithActiveField()
            } catch (e: Exception) {
                Log.e("AdminHomeFragment", "Failed to update existing movies", e)
            }
        }
        
        unsubscribeMovieListener = repository.getMoviesRealtime { movieList ->
            Log.d("AdminHomeFragment", "Received ${movieList.size} movies from listener")
            movieList.forEachIndexed { index, movie ->
                Log.d("AdminHomeFragment", "Movie $index: ${movie.title} (ID: ${movie.id}), isActive: ${movie.isActive}")
            }
            
            // Update both the main list and the filtered list
            allMovies.clear()
            allMovies.addAll(movieList)
            
            // Apply current filters
            filterMovies()
            
            showLoading(false)
            
            if (movieList.isEmpty()) {
                Log.d("AdminHomeFragment", "No movies found, showing empty state")
                showEmptyState(true)
            } else {
                Log.d("AdminHomeFragment", "Movies found: ${movieList.size}, filtering with search='$currentSearchQuery', genre='$currentSelectedGenre'")
                showEmptyState(false)
                Log.d("AdminHomeFragment", "Final movies list after filtering: ${movies.size}")
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
        currentEditingMovie = movie
        selectedImageBitmap = null // Reset selected image
        
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_movie, null)
        
        // Initialize views
        val ivMoviePoster: ImageView = dialogView.findViewById(R.id.ivMoviePoster)
        val btnChangeImage: MaterialButton = dialogView.findViewById(R.id.btnChangeImage)
        val etMovieName: TextInputEditText = dialogView.findViewById(R.id.etMovieName)
        val etDescription: TextInputEditText = dialogView.findViewById(R.id.etDescription)
        val etDirector: TextInputEditText = dialogView.findViewById(R.id.etDirector)
        val etDuration: TextInputEditText = dialogView.findViewById(R.id.etDuration)
        val ratingBar: RatingBar = dialogView.findViewById(R.id.ratingBar)
        val tvRatingValue: MaterialTextView = dialogView.findViewById(R.id.tvRatingValue)
        val spinnerGenre: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerGenre)
        val etMovieTimes: TextInputEditText = dialogView.findViewById(R.id.etMovieTimes)
        val btnCancel: MaterialButton = dialogView.findViewById(R.id.btnCancel)
        val btnSave: MaterialButton = dialogView.findViewById(R.id.btnSave)
        
        editDialogImageView = ivMoviePoster
        
        // Set current values
        loadMovieImageIntoView(movie, ivMoviePoster)
        etMovieName.setText(movie.title)
        etDescription.setText(movie.description)
        etDirector.setText(movie.director)
        etDuration.setText(movie.duration)
        
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
            if (validateInputs(etMovieName, etDescription, etDirector, etDuration, etMovieTimes)) {
                val posterBase64 = selectedImageBitmap?.let { bitmap ->
                    bitmapToBase64(bitmap)
                } ?: movie.posterBase64 // Keep existing image if no new image selected
                
                val updatedMovie = movie.copy(
                    title = etMovieName.text.toString().trim(),
                    description = etDescription.text.toString().trim(),
                    director = etDirector.text.toString().trim(),
                    duration = etDuration.text.toString().trim(),
                    rating = (ratingBar.rating * 2).toDouble(),
                    genre = spinnerGenre.text.toString(),
                    movieTimes = etMovieTimes.text.toString().trim(),
                    posterBase64 = posterBase64
                )
                
                updateMovie(updatedMovie)
                dialog.dismiss()
            }
        }
        
        btnChangeImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        
        dialog.show()
    }
    
    private fun validateInputs(
        etMovieName: TextInputEditText,
        etDescription: TextInputEditText,
        etDirector: TextInputEditText,
        etDuration: TextInputEditText,
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
        
        // Validate director
        if (etDirector.text.toString().trim().isEmpty()) {
            etDirector.error = "Director name is required"
            isValid = false
        } else {
            etDirector.error = null
        }
        
        // Validate duration
        if (etDuration.text.toString().trim().isEmpty()) {
            etDuration.error = "Duration is required"
            isValid = false
        } else {
            etDuration.error = null
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
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirmation, null)
        
        // Create dialog with custom style
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        // Set dialog window properties for rounded corners
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Set movie title in message
        val tvMessage = dialogView.findViewById<MaterialTextView>(R.id.tvDialogMessage)
        tvMessage.text = "Are you sure you want to delete '${movie.title}'? This action cannot be undone."
        
        // Handle button clicks
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<MaterialButton>(R.id.btnDelete)
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnDelete.setOnClickListener {
            dialog.dismiss()
            deleteMovie(movie)
        }
        
        dialog.show()
    }
    
    private fun deleteMovie(movie: Movie) {
        Log.d("AdminHomeFragment", "Starting delete for movie: ${movie.title} (ID: ${movie.id})")
        coroutineScope.launch {
            try {
                val result = repository.deleteMovie(movie.id)
                result.onSuccess {
                    Log.d("AdminHomeFragment", "Delete operation successful for movie: ${movie.title}")
                    Toast.makeText(context, "Movie deleted successfully", Toast.LENGTH_SHORT).show()
                    // Real-time listener will automatically update the UI
                }.onFailure { error ->
                    Log.e("AdminHomeFragment", "Delete operation failed for movie: ${movie.title}", error)
                    Toast.makeText(context, "Failed to delete movie: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AdminHomeFragment", "Exception during delete for movie: ${movie.title}", e)
                Toast.makeText(context, "Error deleting movie: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadMovieImageIntoView(movie: Movie, imageView: ImageView) {
        if (!movie.posterBase64.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(movie.posterBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("AdminHomeFragment", "Error loading movie image", e)
                imageView.setImageResource(R.drawable.atlas) // fallback
            }
        } else {
            imageView.setImageResource(movie.posterResId)
        }
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Use higher compression (lower quality) to reduce size
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        
        // Log the byte array size for debugging
        Log.d("AdminHomeFragment", "Compressed image size: ${byteArray.size} bytes")
        
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        
        // Check if the Base64 string is too large (Firestore limit is ~1MB)
        if (base64String.length > 800000) { // 800KB limit to be safe
            Log.w("AdminHomeFragment", "Base64 string too large: ${base64String.length} characters")
            // Try with even higher compression
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream)
            val smallerByteArray = byteArrayOutputStream.toByteArray()
            Log.d("AdminHomeFragment", "Re-compressed image size: ${smallerByteArray.size} bytes")
            return Base64.encodeToString(smallerByteArray, Base64.DEFAULT)
        }
        
        return base64String
    }
    
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = minOf(scaleWidth, scaleHeight)
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        Log.d("AdminHomeFragment", "Original size: ${width}x${height}, New size: ${newWidth}x${newHeight}")
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}

