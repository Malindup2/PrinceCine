package com.example.princecine.model

data class Seat(
    val id: String,
    val seatNumber: String,
    val row: String,
    val column: Int,
    val isTaken: Boolean,
    var isSelected: Boolean
) {
    fun isEnabled(): Boolean = !isTaken
    
    fun isAvailable(): Boolean = !isTaken && !isSelected
}
