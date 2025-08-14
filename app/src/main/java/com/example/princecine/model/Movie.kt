package com.example.princecine.model

import com.google.firebase.Timestamp

data class Movie(
    val id: String = "", // Firebase document ID
    val title: String = "",
    val description: String = "",
    val genre: String = "",
    val rating: Double = 0.0,
    val duration: String = "",
    val director: String = "",
    val posterUrl: String = "",
    val posterBase64: String? = null,
    val movieTimes: String = "", // Keep for backward compatibility
    val posterResId: Int = 0, // Keep for backward compatibility
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) 