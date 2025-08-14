package com.example.princecine.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirth: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val createdAt: Timestamp? = null,
    val profileImage: String? = null
)

enum class UserRole {
    CUSTOMER, ADMIN
}

