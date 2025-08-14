package com.example.princecine.model

import com.google.firebase.Timestamp

data class Booking(
    val id: String = "", // Firebase document ID
    val bookingId: String = "", // Unique booking reference (e.g., BK12345678901)
    val userId: String = "",
    val movieId: String = "",
    val movieTitle: String = "",
    val moviePosterUrl: String = "",
    val showDate: String = "",
    val showTime: String = "",
    val seats: List<String> = emptyList(), // List of seat numbers like ["A1", "A2"]
    val totalAmount: Double = 0.0,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val paymentMethod: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val qrCodeUrl: String = "",
    val ticketPdfUrl: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

enum class BookingStatus {
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    EXPIRED
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

