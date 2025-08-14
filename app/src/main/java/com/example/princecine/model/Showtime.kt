package com.example.princecine.model

import com.google.firebase.Timestamp

data class Showtime(
    val id: String = "", // Firebase document ID
    val movieId: String = "",
    val showDate: String = "", // Format: "2024-12-15"
    val showTime: String = "", // Format: "2:30 PM"
    val totalSeats: Int = 110,
    val availableSeats: Int = 110,
    val availableSeatsList: List<String> = emptyList(), // Available seat numbers
    val bookedSeats: List<String> = emptyList(), // Booked seat numbers
    val price: Double = 12.99, // Ticket price
    val theatreHall: String = "Hall 1", // Theatre hall identifier
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

