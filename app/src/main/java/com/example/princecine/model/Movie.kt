package com.example.princecine.model

data class Movie(
    val id: Int,
    val title: String,
    val posterResId: Int,
    val rating: String,
    val genre: String,
    val duration: String = "2h 15m",
    val director: String = "Christopher Nolan",
    val description: String = "A mind-bending thriller that explores the concept of time manipulation and reality. When a former CIA agent is given a chance to save the world, he must navigate through a complex web of deception and danger.",
    val movieTimes: String = "2:00 PM, 5:00 PM, 8:00 PM",
    val posterBase64: String? = null
) 