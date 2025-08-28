package com.example.princecine.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.princecine.R
import com.example.princecine.data.FirebaseRepository
import com.example.princecine.model.Movie
import com.example.princecine.ui.SeatSelectionActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MovieDetailsActivity : AppCompatActivity() {
    
    companion object {
        private const val EXTRA_MOVIE_ID = "extra_movie_id"
        private const val EXTRA_MOVIE_TITLE = "extra_movie_title"
        private const val EXTRA_MOVIE_POSTER_BASE64 = "extra_movie_poster_base64"
        private const val EXTRA_MOVIE_RATING = "extra_movie_rating"
        private const val EXTRA_MOVIE_GENRE = "extra_movie_genre"
        private const val EXTRA_MOVIE_DURATION = "extra_movie_duration"
        private const val EXTRA_MOVIE_DESCRIPTION = "extra_movie_description"
        private const val EXTRA_MOVIE_TIMES = "extra_movie_times"
        
        fun newIntent(context: Context, movie: Movie): Intent {
            return Intent(context, MovieDetailsActivity::class.java).apply {
                putExtra(EXTRA_MOVIE_ID, movie.id)
                putExtra(EXTRA_MOVIE_TITLE, movie.title)
                putExtra(EXTRA_MOVIE_POSTER_BASE64, movie.posterBase64)
                putExtra(EXTRA_MOVIE_RATING, movie.rating)
                putExtra(EXTRA_MOVIE_GENRE, movie.genre)
                putExtra(EXTRA_MOVIE_DURATION, movie.duration)
                putExtra(EXTRA_MOVIE_DESCRIPTION, movie.description)
                putExtra(EXTRA_MOVIE_TIMES, movie.movieTimes)
            }
        }
    }
    
    // UI Elements
    private lateinit var ivBackgroundPoster: ImageView
    private lateinit var ivMoviePoster: ImageView
    private lateinit var ivBackButton: ImageView
    private lateinit var ivShareButton: ImageView
    private lateinit var ivMoreOptions: ImageView
    private lateinit var tvMovieTitle: MaterialTextView
    private lateinit var tvDuration: MaterialTextView
    private lateinit var tvGenre: MaterialTextView
    private lateinit var chipRating: Chip
    private lateinit var tvDescription: MaterialTextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var timeChipsContainer: LinearLayout
    
    // Date Selection Chips
    private lateinit var chipDateToday: Chip
    private lateinit var chipDateTomorrow: Chip
    private lateinit var chipDateDay3: Chip
    private lateinit var chipDateDay4: Chip
    private lateinit var chipDateDay5: Chip
    
    // Selected values
    private var selectedDate: String = "Today"
    private var selectedTime: String = ""
    
    // Firebase
    private lateinit var repository: FirebaseRepository
    private var currentMovie: Movie? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)
        
        repository = FirebaseRepository()
        
        initializeViews()
        setupClickListeners()
        loadMovieData()
        setupDateSelection()
        generateDates()
    }
    
    private fun initializeViews() {
        ivBackgroundPoster = findViewById(R.id.ivBackgroundPoster)
        ivMoviePoster = findViewById(R.id.ivMoviePoster)
        ivBackButton = findViewById(R.id.ivBackButton)
        ivShareButton = findViewById(R.id.ivShareButton)
        ivMoreOptions = findViewById(R.id.ivMoreOptions)
        tvMovieTitle = findViewById(R.id.tvMovieTitle)
        tvDuration = findViewById(R.id.tvDuration)
        tvGenre = findViewById(R.id.tvGenre)
        chipRating = findViewById(R.id.chipRating)
        tvDescription = findViewById(R.id.tvDescription)
        btnCheckout = findViewById(R.id.btnCheckout)
        timeChipsContainer = findViewById(R.id.timeChipsContainer)
        
        // Date chips
        chipDateToday = findViewById(R.id.chipDateToday)
        chipDateTomorrow = findViewById(R.id.chipDateTomorrow)
        chipDateDay3 = findViewById(R.id.chipDateDay3)
        chipDateDay4 = findViewById(R.id.chipDateDay4)
        chipDateDay5 = findViewById(R.id.chipDateDay5)
    }
    
    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            finish()
        }
        
        ivShareButton.setOnClickListener {
            shareMovie()
        }
        
        ivMoreOptions.setOnClickListener {
            // TODO: Implement more options menu
            Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show()
        }
        
        btnCheckout.setOnClickListener {
            if (selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select a show time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Get movie data to pass to seat selection
            val movieId = intent.getStringExtra(EXTRA_MOVIE_ID) ?: ""
            val movieTitle = tvMovieTitle.text.toString()
            val moviePosterBase64 = intent.getStringExtra(EXTRA_MOVIE_POSTER_BASE64)
            
            // Navigate to seat selection screen with movie data
            val intent = SeatSelectionActivity.newIntent(
                context = this,
                movieId = movieId,
                movieTitle = movieTitle,
                moviePosterBase64 = moviePosterBase64,
                date = selectedDate,
                time = selectedTime
            )
            startActivity(intent)
        }
    }
    
    private fun shareMovie() {
        val movieTitle = tvMovieTitle.text.toString()
        val shareText = "Check out this movie: $movieTitle\n\nWatch it at PrinceCine!"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share Movie"))
    }
    
    private fun setupDateSelection() {
        val dateChips = listOf(chipDateToday, chipDateTomorrow, chipDateDay3, chipDateDay4, chipDateDay5)
        
        dateChips.forEach { chip ->
            chip.setOnClickListener {
                selectDateChip(chip)
                loadShowTimes() // Load show times when date changes
            }
        }
        
        // Select today by default
        selectDateChip(chipDateToday)
        loadShowTimes()
    }
    
    private fun generateDates() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        
        // Set date texts
        chipDateToday.text = "Today"
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        chipDateTomorrow.text = "Tomorrow"
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        chipDateDay3.text = dateFormat.format(calendar.time)
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        chipDateDay4.text = dateFormat.format(calendar.time)
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        chipDateDay5.text = dateFormat.format(calendar.time)
    }
    
    private fun loadShowTimes() {
        try {
            val movieTimes = intent.getStringExtra(EXTRA_MOVIE_TIMES) ?: ""
            Log.d("MovieDetailsActivity", "Loading show times: $movieTimes")
            
            // Clear existing time chips
            timeChipsContainer.removeAllViews()
            
            if (movieTimes.isNotEmpty()) {
                val times = movieTimes.split(",").map { it.trim() }
                Log.d("MovieDetailsActivity", "Parsed ${times.size} show times")
                
                times.forEach { time ->
                    if (time.isNotEmpty()) {
                        createTimeChip(time)
                    }
                }
            } else {
                Log.w("MovieDetailsActivity", "No show times available")
                Toast.makeText(this, "No show times available for this movie", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MovieDetailsActivity", "Error loading show times", e)
            Toast.makeText(this, "Error loading show times", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun createTimeChip(time: String) {
        val chip = Chip(this).apply {
            text = time
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MovieDetailsActivity, R.color.red))
            setChipBackgroundColorResource(R.color.white)
            chipStrokeColor = ContextCompat.getColorStateList(this@MovieDetailsActivity, R.color.red)
            chipStrokeWidth = 1f
            chipCornerRadius = 20f
            isClickable = true
            isFocusable = true
            
            // Add margin
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = 16
            }
            this.layoutParams = layoutParams
            
            setOnClickListener {
                selectTimeChip(this)
            }
        }
        
        timeChipsContainer.addView(chip)
    }
    
    private fun selectTimeChip(selectedChip: Chip) {
        // Reset all time chips to unselected state
        for (i in 0 until timeChipsContainer.childCount) {
            val child = timeChipsContainer.getChildAt(i) as? Chip
            child?.apply {
                setChipBackgroundColorResource(R.color.white)
                setTextColor(ContextCompat.getColor(this@MovieDetailsActivity, R.color.red))
                chipStrokeColor = ContextCompat.getColorStateList(this@MovieDetailsActivity, R.color.red)
                chipStrokeWidth = 1f
            }
        }
        
        // Set selected chip to selected state
        selectedChip.apply {
            setChipBackgroundColorResource(R.color.red)
            setTextColor(ContextCompat.getColor(this@MovieDetailsActivity, R.color.white))
            chipStrokeWidth = 0f
        }
        
        selectedTime = selectedChip.text.toString()
        Log.d("MovieDetailsActivity", "Selected time: $selectedTime")
    }
    
    private fun selectDateChip(selectedChip: Chip) {
        val allDateChips = listOf(chipDateToday, chipDateTomorrow, chipDateDay3, chipDateDay4, chipDateDay5)
        
        allDateChips.forEach { chip ->
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTextColor(ContextCompat.getColor(this, R.color.red))
            chip.chipStrokeColor = ContextCompat.getColorStateList(this, R.color.red)
            chip.chipStrokeWidth = 1f
        }
        
        selectedChip.setChipBackgroundColorResource(R.color.red)
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedChip.chipStrokeWidth = 0f
        
        selectedDate = selectedChip.text.toString()
        Log.d("MovieDetailsActivity", "Selected date: $selectedDate")
    }
    
    private fun loadMovieData() {
        try {
            val movieId = intent.getStringExtra(EXTRA_MOVIE_ID) ?: ""
            val movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE) ?: "Unknown Movie"
            val moviePosterBase64 = intent.getStringExtra(EXTRA_MOVIE_POSTER_BASE64)
            val movieRating = intent.getDoubleExtra(EXTRA_MOVIE_RATING, 0.0)
            val movieGenre = intent.getStringExtra(EXTRA_MOVIE_GENRE) ?: "Action"
            val movieDuration = intent.getStringExtra(EXTRA_MOVIE_DURATION) ?: "2h 15m"
            val movieDescription = intent.getStringExtra(EXTRA_MOVIE_DESCRIPTION) ?: "No description available."
            
            Log.d("MovieDetailsActivity", "Loading movie: $movieTitle")
            
            // Set movie data
            tvMovieTitle.text = movieTitle
            tvDuration.text = movieDuration
            tvGenre.text = movieGenre
            chipRating.text = String.format("%.1f★", movieRating)
            tvDescription.text = movieDescription
            
            // Load movie poster from Base64
            loadMoviePoster(moviePosterBase64)
            
            Log.d("MovieDetailsActivity", "Movie data loaded successfully")
        } catch (e: Exception) {
            Log.e("MovieDetailsActivity", "Error loading movie data", e)
            Toast.makeText(this, "Error loading movie details", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadMoviePoster(posterBase64: String?) {
        try {
            if (!posterBase64.isNullOrEmpty()) {
                Log.d("MovieDetailsActivity", "Loading poster from Base64")
                val decodedBytes = Base64.decode(posterBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                
                if (bitmap != null) {
                    ivBackgroundPoster.setImageBitmap(bitmap)
                    ivMoviePoster.setImageBitmap(bitmap)
                    Log.d("MovieDetailsActivity", "Poster loaded successfully")
                } else {
                    Log.w("MovieDetailsActivity", "Failed to decode bitmap from Base64")
                    setDefaultPoster()
                }
            } else {
                Log.w("MovieDetailsActivity", "No poster Base64 data available")
                setDefaultPoster()
            }
        } catch (e: Exception) {
            Log.e("MovieDetailsActivity", "Error loading poster", e)
            setDefaultPoster()
        }
    }
    
    private fun setDefaultPoster() {
        ivBackgroundPoster.setImageResource(R.drawable.atlas)
        ivMoviePoster.setImageResource(R.drawable.atlas)
    }
}
