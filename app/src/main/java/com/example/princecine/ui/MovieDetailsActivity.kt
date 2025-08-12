package com.example.princecine.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.princecine.R
import com.example.princecine.model.Movie
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView

class MovieDetailsActivity : AppCompatActivity() {
    
    companion object {
        private const val EXTRA_MOVIE_ID = "extra_movie_id"
        private const val EXTRA_MOVIE_TITLE = "extra_movie_title"
        private const val EXTRA_MOVIE_POSTER = "extra_movie_poster"
        private const val EXTRA_MOVIE_RATING = "extra_movie_rating"
        private const val EXTRA_MOVIE_GENRE = "extra_movie_genre"
        private const val EXTRA_MOVIE_DURATION = "extra_movie_duration"
        private const val EXTRA_MOVIE_DIRECTOR = "extra_movie_director"
        private const val EXTRA_MOVIE_DESCRIPTION = "extra_movie_description"
        
        fun newIntent(context: Context, movie: Movie): Intent {
            return Intent(context, MovieDetailsActivity::class.java).apply {
                putExtra(EXTRA_MOVIE_ID, movie.id)
                putExtra(EXTRA_MOVIE_TITLE, movie.title)
                putExtra(EXTRA_MOVIE_POSTER, movie.posterResId)
                putExtra(EXTRA_MOVIE_RATING, movie.rating)
                putExtra(EXTRA_MOVIE_GENRE, movie.genre)
                putExtra(EXTRA_MOVIE_DURATION, movie.duration)
                putExtra(EXTRA_MOVIE_DIRECTOR, movie.director)
                putExtra(EXTRA_MOVIE_DESCRIPTION, movie.description)
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
    private lateinit var tvDirector: MaterialTextView
    private lateinit var tvGenre: MaterialTextView
    private lateinit var chipRating: Chip
    private lateinit var tvDescription: MaterialTextView
    private lateinit var btnCheckout: MaterialButton
    
    // Date Selection Chips
    private lateinit var chipDateToday: Chip
    private lateinit var chipDateTomorrow: Chip
    private lateinit var chipDateDay3: Chip
    private lateinit var chipDateDay4: Chip
    private lateinit var chipDateDay5: Chip
    
    // Time Selection Chips
    private lateinit var chipTime1: Chip
    private lateinit var chipTime2: Chip
    private lateinit var chipTime3: Chip
    private lateinit var chipTime4: Chip
    private lateinit var chipTime5: Chip
    private lateinit var chipTime6: Chip
    
    // Selected values
    private var selectedDate: String = "Today"
    private var selectedTime: String = "12:30 PM"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)
        
        initializeViews()
        setupClickListeners()
        loadMovieData()
        setupDateSelection()
        setupTimeSelection()
    }
    
    private fun initializeViews() {
        ivBackgroundPoster = findViewById(R.id.ivBackgroundPoster)
        ivMoviePoster = findViewById(R.id.ivMoviePoster)
        ivBackButton = findViewById(R.id.ivBackButton)
        ivShareButton = findViewById(R.id.ivShareButton)
        ivMoreOptions = findViewById(R.id.ivMoreOptions)
        tvMovieTitle = findViewById(R.id.tvMovieTitle)
        tvDuration = findViewById(R.id.tvDuration)
        tvDirector = findViewById(R.id.tvDirector)
        tvGenre = findViewById(R.id.tvGenre)
        chipRating = findViewById(R.id.chipRating)
        tvDescription = findViewById(R.id.tvDescription)
        btnCheckout = findViewById(R.id.btnCheckout)
        
        // Date chips
        chipDateToday = findViewById(R.id.chipDateToday)
        chipDateTomorrow = findViewById(R.id.chipDateTomorrow)
        chipDateDay3 = findViewById(R.id.chipDateDay3)
        chipDateDay4 = findViewById(R.id.chipDateDay4)
        chipDateDay5 = findViewById(R.id.chipDateDay5)
        
        // Time chips
        chipTime1 = findViewById(R.id.chipTime1)
        chipTime2 = findViewById(R.id.chipTime2)
        chipTime3 = findViewById(R.id.chipTime3)
        chipTime4 = findViewById(R.id.chipTime4)
        chipTime5 = findViewById(R.id.chipTime5)
        chipTime6 = findViewById(R.id.chipTime6)
    }
    
    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            finish()
        }
        
        ivShareButton.setOnClickListener {
            // TODO: Implement share functionality
            Toast.makeText(this, "Share movie", Toast.LENGTH_SHORT).show()
        }
        
        ivMoreOptions.setOnClickListener {
            // TODO: Implement more options menu
            Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show()
        }
        
        btnCheckout.setOnClickListener {
            // TODO: Navigate to booking screen with selected date and time
            Toast.makeText(this, "Booking for $selectedDate at $selectedTime", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupDateSelection() {
        val dateChips = listOf(chipDateToday, chipDateTomorrow, chipDateDay3, chipDateDay4, chipDateDay5)
        
        dateChips.forEach { chip ->
            chip.setOnClickListener {
                selectDateChip(chip)
            }
        }
    }
    
    private fun setupTimeSelection() {
        val timeChips = listOf(chipTime1, chipTime2, chipTime3, chipTime4, chipTime5, chipTime6)
        
        timeChips.forEach { chip ->
            chip.setOnClickListener {
                selectTimeChip(chip)
            }
        }
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
    }
    
    private fun selectTimeChip(selectedChip: Chip) {
        val allTimeChips = listOf(chipTime1, chipTime2, chipTime3, chipTime4, chipTime5, chipTime6)
        
        allTimeChips.forEach { chip ->
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTextColor(ContextCompat.getColor(this, R.color.red))
            chip.chipStrokeColor = ContextCompat.getColorStateList(this, R.color.red)
            chip.chipStrokeWidth = 1f
        }
        
        selectedChip.setChipBackgroundColorResource(R.color.red)
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedChip.chipStrokeWidth = 0f
        
        selectedTime = selectedChip.text.toString()
    }
    
    private fun loadMovieData() {
        val movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE) ?: "Unknown Movie"
        val moviePoster = intent.getIntExtra(EXTRA_MOVIE_POSTER, R.drawable.atlas)
        val movieRating = intent.getStringExtra(EXTRA_MOVIE_RATING) ?: "N/A"
        val movieGenre = intent.getStringExtra(EXTRA_MOVIE_GENRE) ?: "Action"
        val movieDuration = intent.getStringExtra(EXTRA_MOVIE_DURATION) ?: "2h 15m"
        val movieDirector = intent.getStringExtra(EXTRA_MOVIE_DIRECTOR) ?: "Unknown Director"
        val movieDescription = intent.getStringExtra(EXTRA_MOVIE_DESCRIPTION) ?: "No description available."
        
        // Set movie data
        tvMovieTitle.text = movieTitle
        ivBackgroundPoster.setImageResource(moviePoster)
        ivMoviePoster.setImageResource(moviePoster)
        tvDuration.text = movieDuration
        tvDirector.text = movieDirector
        tvGenre.text = movieGenre
        chipRating.text = movieRating
        tvDescription.text = movieDescription
    }
}
