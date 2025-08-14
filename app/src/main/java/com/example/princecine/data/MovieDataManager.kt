package com.example.princecine.data

import com.example.princecine.model.Movie

object MovieDataManager {
    
    private val movies = mutableListOf<Movie>()
    
    init {
        // Initialize with some default movies
        movies.addAll(getDefaultMovies())
    }
    
    fun getAllMovies(): List<Movie> {
        return movies.toList()
    }
    
    fun addMovie(movie: Movie) {
        movies.add(movie)
    }
    
    fun getMoviesByGenre(genre: String): List<Movie> {
        return movies.filter { it.genre.equals(genre, ignoreCase = true) }
    }
    
    fun searchMovies(query: String): List<Movie> {
        return movies.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.description.contains(query, ignoreCase = true) ||
            it.genre.contains(query, ignoreCase = true)
        }
    }
    
    private fun getDefaultMovies(): List<Movie> {
        return listOf(
            Movie(
                id = "1",
                title = "Atlas",
                posterResId = com.example.princecine.R.drawable.atlas,
                rating = 4.2,
                genre = "Sci-Fi",
                duration = "2h 15m",
                director = "Brad Peyton",
                description = "A mind-bending thriller that explores the concept of time manipulation and reality. When a former CIA agent is given a chance to save the world, he must navigate through a complex web of deception and danger.",
                movieTimes = "2:00 PM, 5:00 PM, 8:00 PM"
            ),
            Movie(
                id = "2",
                title = "Bad Boys",
                posterResId = com.example.princecine.R.drawable.bad_boys,
                rating = 4.5,
                genre = "Action",
                duration = "2h 5m",
                director = "Adil El Arbi",
                description = "The Bad Boys Mike Lowrey and Marcus Burnett are back together for one last ride in the fourth installment of the Bad Boys franchise.",
                movieTimes = "1:30 PM, 4:30 PM, 7:30 PM"
            ),
            Movie(
                id = "3",
                title = "Dune: Part Two",
                posterResId = com.example.princecine.R.drawable.dube2,
                rating = 4.8,
                genre = "Sci-Fi",
                duration = "2h 46m",
                director = "Denis Villeneuve",
                description = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
                movieTimes = "12:00 PM, 3:00 PM, 6:00 PM, 9:00 PM"
            ),
            Movie(
                id = "4",
                title = "Fly Me to the Moon",
                posterResId = com.example.princecine.R.drawable.fly_me_to_the_moon,
                rating = 3.9,
                genre = "Comedy",
                duration = "1h 52m",
                director = "Greg Berlanti",
                description = "A marketing executive is hired to help NASA sell the Apollo 11 moon landing to the American public.",
                movieTimes = "2:15 PM, 5:15 PM, 8:15 PM"
            ),
            Movie(
                id = "5",
                title = "The Fall Guy",
                posterResId = com.example.princecine.R.drawable.the_fall_guy,
                rating = 4.1,
                genre = "Action",
                duration = "2h 6m",
                director = "David Leitch",
                description = "A stuntman who gets fired from the biggest movie of his career and finds himself working on a set with his ex-girlfriend.",
                movieTimes = "1:45 PM, 4:45 PM, 7:45 PM"
            )
        )
    }
}
