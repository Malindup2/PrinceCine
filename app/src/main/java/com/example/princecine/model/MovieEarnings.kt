package com.example.princecine.model

data class MovieEarnings(
    val movieId: String = "",
    val movieTitle: String = "",
    val posterUrl: String = "",
    val posterBase64: String? = null,
    val posterResId: Int = 0, // Keep for backwards compatibility
    val ticketsSold: Int = 0,
    val totalEarnings: Double = 0.0
)
