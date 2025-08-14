package com.example.princecine.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.adapter.MovieAdapter
import com.example.princecine.model.Movie
import com.example.princecine.ui.MovieDetailsActivity
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText

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
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupSearchListener()
        setupCapsuleClickListeners()
        setupRecyclerView()
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
        val movies = getSampleMovies()
        val adapter = MovieAdapter(movies) { movie ->
            // Navigate to movie details
            val intent = MovieDetailsActivity.newIntent(requireContext(), movie)
            startActivity(intent)
        }
        
        rvMovies.layoutManager = GridLayoutManager(context, 2)
        rvMovies.adapter = adapter
    }
    
    private fun getSampleMovies(): List<Movie> {
        return listOf(
            Movie(
                id = "1",
                title = "The Fall Guy",
                posterResId = R.drawable.the_fall_guy,
                rating = 8.2,
                genre = "Action",
                duration = "2h 6m",
                director = "David Leitch",
                description = "A down-and-out stuntman must find the missing star of his ex-girlfriend's blockbuster film."
            ),
            Movie(
                id = "2",
                title = "Fly Me to the Moon",
                posterResId = R.drawable.fly_me_to_the_moon,
                rating = 7.8,
                genre = "Sci-Fi",
                duration = "1h 52m",
                director = "Greg Berlanti",
                description = "A marketing executive is hired to help NASA sell the Apollo 11 moon landing to the American public."
            ),
            Movie(
                id = "3",
                title = "Dune: Part Two",
                posterResId = R.drawable.dube2,
                rating = 8.5,
                genre = "Sci-Fi",
                duration = "2h 46m",
                director = "Denis Villeneuve",
                description = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family."
            ),
            Movie(
                id = "4",
                title = "Bad Boys: Ride or Die",
                posterResId = R.drawable.bad_boys,
                rating = 7.9,
                genre = "Action",
                duration = "1h 55m",
                director = "Adil El Arbi",
                description = "Detectives Mike Lowrey and Marcus Burnett investigate corruption within the Miami Police Department."
            ),
            Movie(
                id = "5",
                title = "Atlas",
                posterResId = R.drawable.atlas,
                rating = 7.5,
                genre = "Sci-Fi",
                duration = "1h 58m",
                director = "Brad Peyton",
                description = "A brilliant counterterrorism analyst with a deep distrust of AI discovers it might be her only hope when a mission to capture a renegade robot goes awry."
            ),
            Movie(
                id = "6",
                title = "The Fall Guy",
                posterResId = R.drawable.the_fall_guy,
                rating = 8.2,
                genre = "Action",
                duration = "2h 6m",
                director = "David Leitch",
                description = "A down-and-out stuntman must find the missing star of his ex-girlfriend's blockbuster film."
            ),
            Movie(
                id = "7",
                title = "Fly Me to the Moon",
                posterResId = R.drawable.fly_me_to_the_moon,
                rating = 7.8,
                genre = "Sci-Fi",
                duration = "1h 52m",
                director = "Greg Berlanti",
                description = "A marketing executive is hired to help NASA sell the Apollo 11 moon landing to the American public."
            ),
            Movie(
                id = "8",
                title = "Dune: Part Two",
                posterResId = R.drawable.dube2,
                rating = 8.5,
                genre = "Sci-Fi",
                duration = "2h 46m",
                director = "Denis Villeneuve",
                description = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family."
            )
        )
    }
} 