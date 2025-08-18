package com.example.princecine.data

import com.example.princecine.model.Movie

object MovieDataManager {
    
    @Deprecated("This class is deprecated. Use FirebaseRepository for movie data operations.")
    private val movies = mutableListOf<Movie>()
    
    @Deprecated("Use FirebaseRepository.getMovies() instead")
    fun getAllMovies(): List<Movie> {
        return emptyList() // Return empty list to prevent dummy data usage
    }
    
    @Deprecated("Use FirebaseRepository.addMovie() instead")
    fun addMovie(movie: Movie) {
        // No-op - redirect to Firebase
    }
    
    @Deprecated("Use FirebaseRepository.getMoviesByGenre() instead")
    fun getMoviesByGenre(genre: String): List<Movie> {
        return emptyList() // Return empty list to prevent dummy data usage
    }
    
    @Deprecated("Use FirebaseRepository.searchMovies() instead")
    fun searchMovies(query: String): List<Movie> {
        return emptyList() // Return empty list to prevent dummy data usage
    }
}
