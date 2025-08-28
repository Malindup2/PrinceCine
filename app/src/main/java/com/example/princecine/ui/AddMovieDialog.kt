package com.example.princecine.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.example.princecine.R
import com.example.princecine.model.Movie
import com.example.princecine.data.FirebaseRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddMovieDialog(private val activity: FragmentActivity) {
    
    private val repository = FirebaseRepository()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    private lateinit var dialog: AlertDialog
    private lateinit var dialogView: View
    
    // Form fields
    private lateinit var ivMoviePoster: ImageView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var etMovieName: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDirector: TextInputEditText
    private lateinit var etDuration: TextInputEditText
    private lateinit var etTime: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var chipGroup: ChipGroup
    
    // Validation fields
    private lateinit var tilMovieName: TextInputLayout
    private lateinit var tilDescription: TextInputLayout
    private lateinit var tilDirector: TextInputLayout
    private lateinit var tilDuration: TextInputLayout
    private lateinit var tilTime: TextInputLayout
    
    private var selectedImageUri: Uri? = null
    private var selectedImageBase64: String? = null
    private var selectedGenre: String? = null
    
    // Image picker request code
    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }
    
    fun show(onMovieAdded: (Movie) -> Unit) {
        try {
            createDialog()
            setupViews()
            setupListeners(onMovieAdded)
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(activity, "Error opening dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun createDialog() {
        try {
            val builder = AlertDialog.Builder(activity)
            dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_movie, null)
            
            dialog = builder
                .setView(dialogView)
                .setCancelable(false)
                .create()
        } catch (e: Exception) {
            Toast.makeText(activity, "Error creating dialog: ${e.message}", Toast.LENGTH_SHORT).show()
            throw e
        }
    }
    
    private fun setupViews() {
        try {
            ivMoviePoster = dialogView.findViewById(R.id.ivMoviePoster)
            btnSelectImage = dialogView.findViewById(R.id.btnSelectImage)
            etMovieName = dialogView.findViewById(R.id.etMovieName)
            etDescription = dialogView.findViewById(R.id.etDescription)
            etDirector = dialogView.findViewById(R.id.etDirector)
            etDuration = dialogView.findViewById(R.id.etDuration)
            etTime = dialogView.findViewById(R.id.etTime)
            ratingBar = dialogView.findViewById(R.id.ratingBar)
            chipGroup = dialogView.findViewById(R.id.chipGroup)
            
            tilMovieName = dialogView.findViewById(R.id.tilMovieName)
            tilDescription = dialogView.findViewById(R.id.tilDescription)
            tilDirector = dialogView.findViewById(R.id.tilDirector)
            tilDuration = dialogView.findViewById(R.id.tilDuration)
            tilTime = dialogView.findViewById(R.id.tilTime)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error setting up views: ${e.message}", Toast.LENGTH_SHORT).show()
            throw e
        }
    }
    
    private fun setupListeners(onMovieAdded: (Movie) -> Unit) {
        try {
            btnSelectImage.setOnClickListener {
                openImagePicker()
            }
            
            // Setup genre chip listeners
            setupGenreChips()
            
            // Setup buttons
            dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            
            dialogView.findViewById<MaterialButton>(R.id.btnSubmit).setOnClickListener {
                if (validateForm()) {
                    val movie = createMovie()
                    submitMovieToFirebase(movie, onMovieAdded)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Error setting up listeners: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupGenreChips() {
        try {
            val chips = listOf(
                dialogView.findViewById<Chip>(R.id.chipAction),
                dialogView.findViewById<Chip>(R.id.chipDrama),
                dialogView.findViewById<Chip>(R.id.chipComedy),
                dialogView.findViewById<Chip>(R.id.chipThriller),
                dialogView.findViewById<Chip>(R.id.chipSciFi),
                dialogView.findViewById<Chip>(R.id.chipHorror)
            )
            
            chips.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // Uncheck other chips
                        chips.forEach { otherChip ->
                            if (otherChip != chip) {
                                otherChip.isChecked = false
                            }
                        }
                        selectedGenre = chip.text.toString()
                    } else {
                        selectedGenre = null
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Error setting up genre chips: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activity.startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error opening image picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Method to handle activity result
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            selectedImageUri?.let {
                this.selectedImageUri = it
                displaySelectedImage(it)
            }
        }
    }
    
    private fun displaySelectedImage(uri: Uri) {
        try {
            val inputStream = activity.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            
            // Resize bitmap to reduce size - max 400x600 pixels for poster
            val resizedBitmap = resizeBitmap(originalBitmap, 400, 600)
            
            ivMoviePoster.setImageBitmap(resizedBitmap)
            ivMoviePoster.alpha = 1.0f
            btnSelectImage.text = "Change Image"
            
            // Convert resized bitmap to Base64 for storage
            selectedImageBase64 = bitmapToBase64(resizedBitmap)
            
            // Log the size for debugging
            android.util.Log.d("AddMovieDialog", "Base64 image size: ${selectedImageBase64?.length ?: 0} characters")
            
        } catch (e: Exception) {
            android.util.Log.e("AddMovieDialog", "Error loading image", e)
            Toast.makeText(activity, "Error loading image", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = minOf(scaleWidth, scaleHeight)
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Use higher compression (lower quality) to reduce size further
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        
        // Log the byte array size for debugging
        android.util.Log.d("AddMovieDialog", "Compressed image size: ${byteArray.size} bytes")
        
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        
        // Check if the Base64 string is too large (Firestore limit is ~1MB)
        if (base64String.length > 800000) { // 800KB limit to be safe
            android.util.Log.w("AddMovieDialog", "Base64 string too large: ${base64String.length} characters")
            // Try with even higher compression
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream)
            val smallerByteArray = byteArrayOutputStream.toByteArray()
            android.util.Log.d("AddMovieDialog", "Re-compressed image size: ${smallerByteArray.size} bytes")
            return Base64.encodeToString(smallerByteArray, Base64.DEFAULT)
        }
        
        return base64String
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Validate movie name
        val movieName = etMovieName.text.toString().trim()
        if (movieName.isEmpty()) {
            tilMovieName.error = "Movie name is required"
            isValid = false
        } else {
            tilMovieName.error = null
        }
        
        // Validate description
        val description = etDescription.text.toString().trim()
        if (description.isEmpty()) {
            tilDescription.error = "Description is required"
            isValid = false
        } else {
            tilDescription.error = null
        }
        
        // Validate director
        val director = etDirector.text.toString().trim()
        if (director.isEmpty()) {
            tilDirector.error = "Director name is required"
            isValid = false
        } else {
            tilDirector.error = null
        }
        
        // Validate duration
        val duration = etDuration.text.toString().trim()
        if (duration.isEmpty()) {
            tilDuration.error = "Duration is required"
            isValid = false
        } else {
            tilDuration.error = null
        }
        
        // Validate time
        val time = etTime.text.toString().trim()
        if (time.isEmpty()) {
            tilTime.error = "Showtimes are required"
            isValid = false
        } else {
            tilTime.error = null
        }
        
        // Validate genre
        if (selectedGenre == null) {
            Toast.makeText(activity, "Please select a genre", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        // Validate rating
        val rating = ratingBar.rating
        if (rating < 1) {
            Toast.makeText(activity, "Please set a rating", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        // Validate image (optional for now)
        if (selectedImageBase64 == null) {
            // For now, we'll make image optional to avoid crashes
            // Toast.makeText(activity, "Please select a movie poster", Toast.LENGTH_SHORT).show()
            // isValid = false
        }
        
        return isValid
    }
    
    private fun createMovie(): Movie {
        return Movie(
            title = etMovieName.text.toString().trim(),
            description = etDescription.text.toString().trim(),
            genre = selectedGenre ?: "Action",
            rating = ratingBar.rating.toDouble(),
            duration = etDuration.text.toString().trim(),
            director = etDirector.text.toString().trim(),
            movieTimes = etTime.text.toString().trim(),
            posterBase64 = selectedImageBase64,
            isActive = true // Explicitly set to ensure movie appears in lists
        )
    }
    
    private fun submitMovieToFirebase(movie: Movie, onMovieAdded: (Movie) -> Unit) {
        coroutineScope.launch {
            try {
                // Show loading state
                val submitButton = dialogView.findViewById<MaterialButton>(R.id.btnSubmit)
                submitButton.isEnabled = false
                submitButton.text = "Adding..."
                
                var movieToSubmit = movie
                
                // Add movie to Firestore (image is already stored as Base64 in the movie object)
                Log.d("AddMovieDialog", "Submitting movie: ${movieToSubmit.title}")
                Log.d("AddMovieDialog", "Movie genre: ${movieToSubmit.genre}")
                Log.d("AddMovieDialog", "Movie rating: ${movieToSubmit.rating}")
                Log.d("AddMovieDialog", "Movie isActive: ${movieToSubmit.isActive}")
                Log.d("AddMovieDialog", "Movie has poster: ${!movieToSubmit.posterBase64.isNullOrEmpty()}")
                
                val result = repository.addMovie(movieToSubmit)
                
                withContext(Dispatchers.Main) {
                    result.onSuccess { movieId ->
                        Log.d("AddMovieDialog", "Movie added successfully with ID: $movieId")
                        val savedMovie = movieToSubmit.copy(id = movieId)
                        onMovieAdded(savedMovie)
                        dialog.dismiss()
                        Toast.makeText(activity, "Movie added successfully!", Toast.LENGTH_SHORT).show()
                    }.onFailure { error ->
                        Log.e("AddMovieDialog", "Failed to add movie: ${error.message}", error)
                        Toast.makeText(activity, "Failed to add movie: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                    
                    // Reset button state
                    submitButton.isEnabled = true
                    submitButton.text = "Add Movie"
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    
                    // Reset button state
                    val submitButton = dialogView.findViewById<MaterialButton>(R.id.btnSubmit)
                    submitButton.isEnabled = true
                    submitButton.text = "Add Movie"
                }
            }
        }
    }
}
