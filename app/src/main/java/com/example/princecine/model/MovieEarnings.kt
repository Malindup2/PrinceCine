package com.example.princecine.model

data class MovieEarnings(
    val movieId: Int,
    val movieTitle: String,
    val posterResId: Int,
    val ticketsSold: Int,
    val totalEarnings: Double
)
