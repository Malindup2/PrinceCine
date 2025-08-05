package com.example.princecine.ui.fragments

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
            // Handle movie click
            Toast.makeText(context, "Selected: ${movie.title}", Toast.LENGTH_SHORT).show()
        }
        
        rvMovies.layoutManager = GridLayoutManager(context, 2)
        rvMovies.adapter = adapter
    }
    
    private fun getSampleMovies(): List<Movie> {
        return listOf(
            Movie(1, "The Fall Guy", R.drawable.the_fall_guy, "8.2/10", "Action"),
            Movie(2, "Fly Me to the Moon", R.drawable.fly_me_to_the_moon, "7.8/10", "Sci-Fi"),
            Movie(3, "Dune: Part Two", R.drawable.dube2, "8.5/10", "Sci-Fi"),
            Movie(4, "Bad Boys: Ride or Die", R.drawable.bad_boys, "7.9/10", "Action"),
            Movie(5, "Atlas", R.drawable.atlas, "7.5/10", "Sci-Fi"),
            Movie(6, "The Fall Guy", R.drawable.the_fall_guy, "8.2/10", "Action"),
            Movie(7, "Fly Me to the Moon", R.drawable.fly_me_to_the_moon, "7.8/10", "Sci-Fi"),
            Movie(8, "Dune: Part Two", R.drawable.dube2, "8.5/10", "Sci-Fi")
        )
    }
} 