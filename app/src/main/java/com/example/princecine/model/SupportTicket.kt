package com.example.princecine.model

data class SupportTicket(
    val id: String,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val dateRaised: String,
    val ticketId: String
)

enum class TicketStatus {
    PENDING,
    RESOLVED
}
