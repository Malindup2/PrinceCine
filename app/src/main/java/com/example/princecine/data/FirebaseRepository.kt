package com.example.princecine.data

import android.net.Uri
import com.example.princecine.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    // Authentication
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = getUserById(result.user?.uid ?: "")
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signUp(user: User, password: String): Result<User> {
        return try {
            android.util.Log.d("FirebaseRepository", "Starting user registration for email: ${user.email}")
            
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val userId = result.user?.uid ?: throw Exception("Failed to create user - no user ID returned")
            
            android.util.Log.d("FirebaseRepository", "User created in Firebase Auth with ID: $userId")
            
            val newUser = user.copy(
                id = userId,
                createdAt = Timestamp.now()
            )
            
            android.util.Log.d("FirebaseRepository", "Saving user data to Firestore")
            try {
                db.collection("users").document(userId).set(newUser).await()
                android.util.Log.d("FirebaseRepository", "User data saved to Firestore successfully")
            } catch (firestoreException: Exception) {
                android.util.Log.e("FirebaseRepository", "Firestore write failed", firestoreException)
                throw Exception("Failed to save user data: ${firestoreException.message}")
            }
            
            android.util.Log.d("FirebaseRepository", "User registration completed successfully")
            Result.success(newUser)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "User registration failed", e)
            Result.failure(e)
        }
    }
    
    suspend fun signOut() {
        auth.signOut()
    }
    
    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            getUserById(firebaseUser.uid)
        } else null
    }
    
    // Users
    suspend fun getUserById(userId: String): User? {
        return try {
            val document = db.collection("users").document(userId).get().await()
            document.toObject(User::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            db.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = db.collection("users").get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Movies
    suspend fun getMovies(): Result<List<Movie>> {
        return try {
            val snapshot = db.collection("movies")
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val movies = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Movie::class.java)?.copy(id = doc.id)
            }
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMovieById(movieId: String): Movie? {
        return try {
            val document = db.collection("movies").document(movieId).get().await()
            document.toObject(Movie::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getMoviesByGenre(genre: String): Result<List<Movie>> {
        return try {
            val snapshot = db.collection("movies")
                .whereEqualTo("genre", genre)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val movies = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Movie::class.java)?.copy(id = doc.id)
            }
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchMovies(query: String): Result<List<Movie>> {
        return try {
            val movies = getMovies().getOrNull() ?: emptyList()
            val filteredMovies = movies.filter { movie ->
                movie.title.contains(query, ignoreCase = true) ||
                movie.description.contains(query, ignoreCase = true) ||
                movie.genre.contains(query, ignoreCase = true) ||
                movie.director.contains(query, ignoreCase = true)
            }
            Result.success(filteredMovies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addMovie(movie: Movie): Result<String> {
        return try {
            val movieData = movie.copy(
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            val docRef = db.collection("movies").add(movieData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateMovie(movie: Movie): Result<Unit> {
        return try {
            val movieData = movie.copy(updatedAt = Timestamp.now())
            db.collection("movies").document(movie.id).set(movieData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteMovie(movieId: String): Result<Unit> {
        return try {
            // Soft delete by setting isActive to false
            db.collection("movies").document(movieId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Showtimes
    suspend fun getShowtimes(movieId: String): Result<List<Showtime>> {
        return try {
            val snapshot = db.collection("showtimes")
                .whereEqualTo("movieId", movieId)
                .whereEqualTo("isActive", true)
                .orderBy("showDate")
                .orderBy("showTime")
                .get()
                .await()
            
            val showtimes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Showtime::class.java)?.copy(id = doc.id)
            }
            Result.success(showtimes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getShowtimeById(showtimeId: String): Showtime? {
        return try {
            val document = db.collection("showtimes").document(showtimeId).get().await()
            document.toObject(Showtime::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun addShowtime(showtime: Showtime): Result<String> {
        return try {
            val showtimeData = showtime.copy(
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            val docRef = db.collection("showtimes").add(showtimeData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateShowtime(showtime: Showtime): Result<Unit> {
        return try {
            val showtimeData = showtime.copy(updatedAt = Timestamp.now())
            db.collection("showtimes").document(showtime.id).set(showtimeData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Bookings
    suspend fun createBooking(booking: Booking): Result<String> {
        return try {
            val bookingData = booking.copy(
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            val docRef = db.collection("bookings").add(bookingData).await()
            
            // Update showtime to mark seats as booked
            if (booking.movieId.isNotEmpty()) {
                updateBookedSeats(booking.movieId, booking.showDate, booking.showTime, booking.seats)
            }
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun updateBookedSeats(movieId: String, showDate: String, showTime: String, newBookedSeats: List<String>) {
        try {
            val snapshot = db.collection("showtimes")
                .whereEqualTo("movieId", movieId)
                .whereEqualTo("showDate", showDate)
                .whereEqualTo("showTime", showTime)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.let { doc ->
                val showtime = doc.toObject(Showtime::class.java)
                showtime?.let {
                    val updatedBookedSeats = it.bookedSeats + newBookedSeats
                    val updatedAvailableSeats = it.availableSeatsList - newBookedSeats.toSet()
                    
                    db.collection("showtimes").document(doc.id)
                        .update(
                            mapOf(
                                "bookedSeats" to updatedBookedSeats,
                                "availableSeatsList" to updatedAvailableSeats,
                                "availableSeats" to updatedAvailableSeats.size,
                                "updatedAt" to Timestamp.now()
                            )
                        ).await()
                }
            }
        } catch (e: Exception) {
            // Log error but don't throw - booking is still valid
            android.util.Log.e("FirebaseRepository", "Error updating booked seats", e)
        }
    }
    
    suspend fun getUserBookings(userId: String): Result<List<Booking>> {
        return try {
            val snapshot = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val bookings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)?.copy(id = doc.id)
            }
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllBookings(): Result<List<Booking>> {
        return try {
            val snapshot = db.collection("bookings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val bookings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)?.copy(id = doc.id)
            }
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBookingById(bookingId: String): Booking? {
        return try {
            val document = db.collection("bookings").document(bookingId).get().await()
            document.toObject(Booking::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit> {
        return try {
            db.collection("bookings").document(bookingId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePaymentStatus(bookingId: String, paymentStatus: PaymentStatus): Result<Unit> {
        return try {
            db.collection("bookings").document(bookingId)
                .update(
                    mapOf(
                        "paymentStatus" to paymentStatus,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        return try {
            // Update booking status
            db.collection("bookings").document(bookingId)
                .update(
                    mapOf(
                        "status" to BookingStatus.CANCELLED,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            
            // TODO: Free up the booked seats in showtime
            // This would require getting the booking details and updating the showtime
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Analytics and Earnings
    suspend fun getMovieEarnings(): Result<List<MovieEarnings>> {
        return try {
            val bookings = getAllBookings().getOrNull() ?: emptyList()
            val completedBookings = bookings.filter { it.status == BookingStatus.COMPLETED }
            
            val earningsMap = completedBookings.groupBy { it.movieId }
                .mapValues { (_, bookingList) ->
                    val totalTickets = bookingList.sumOf { it.seats.size }
                    val totalEarnings = bookingList.sumOf { it.totalAmount }
                    Pair(totalTickets, totalEarnings)
                }
            
            val movieEarnings = mutableListOf<MovieEarnings>()
            earningsMap.forEach { (movieId, earnings) ->
                val movie = getMovieById(movieId)
                movie?.let {
                    movieEarnings.add(
                        MovieEarnings(
                            movieId = movieId.hashCode(), // Convert string to int for backwards compatibility
                            movieTitle = it.title,
                            posterResId = it.posterResId, // For backwards compatibility
                            ticketsSold = earnings.first,
                            totalEarnings = earnings.second
                        )
                    )
                }
            }
            
            Result.success(movieEarnings.sortedByDescending { it.totalEarnings })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Support Tickets
    suspend fun createSupportTicket(ticket: SupportTicket): Result<String> {
        return try {
            val ticketData = ticket.copy(
                dateRaised = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            val docRef = db.collection("support_tickets").add(ticketData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserTickets(userId: String): Result<List<SupportTicket>> {
        return try {
            val snapshot = db.collection("support_tickets")
                .whereEqualTo("userId", userId)
                .orderBy("dateRaised", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val tickets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SupportTicket::class.java)?.copy(id = doc.id)
            }
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllSupportTickets(): Result<List<SupportTicket>> {
        return try {
            val snapshot = db.collection("support_tickets")
                .orderBy("dateRaised", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val tickets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SupportTicket::class.java)?.copy(id = doc.id)
            }
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTicketStatus(ticketId: String, status: TicketStatus): Result<Unit> {
        return try {
            val updateData = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to Timestamp.now()
            )
            
            if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
                updateData["resolvedAt"] = Timestamp.now()
            }
            
            db.collection("support_tickets").document(ticketId)
                .update(updateData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTicketResolution(ticketId: String, resolution: String, adminNotes: String = ""): Result<Unit> {
        return try {
            db.collection("support_tickets").document(ticketId)
                .update(
                    mapOf(
                        "resolution" to resolution,
                        "adminNotes" to adminNotes,
                        "status" to TicketStatus.RESOLVED,
                        "updatedAt" to Timestamp.now(),
                        "resolvedAt" to Timestamp.now()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Storage
    suspend fun uploadImage(imageUri: Uri, path: String): Result<String> {
        return try {
            val storageRef = storage.reference.child(path)
            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadBase64Image(base64String: String, path: String): Result<String> {
        return try {
            val storageRef = storage.reference.child(path)
            val bytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            val uploadTask = storageRef.putBytes(bytes).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Utility functions
    fun generateBookingId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "BK${timestamp}${random}"
    }
    
    fun generateTicketId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "ST${timestamp}${random}"
    }
    
    // Initialize default data (call this once when app is first launched)
    suspend fun initializeDefaultData() {
        try {
            // Check if movies collection is empty
            val snapshot = db.collection("movies").limit(1).get().await()
            if (snapshot.isEmpty) {
                // Add default movies
                addDefaultMovies()
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error initializing default data", e)
        }
    }
    
    private suspend fun addDefaultMovies() {
        val defaultMovies = listOf(
            Movie(
                title = "Atlas",
                description = "A brilliant counterterrorism analyst with a deep distrust of AI discovers it might be her only hope when a mission to capture a renegade robot goes awry.",
                genre = "Sci-Fi",
                rating = 4.2,
                duration = "2h 15m",
                director = "Brad Peyton",
                movieTimes = "2:00 PM, 5:00 PM, 8:00 PM",
                isActive = true
            ),
            Movie(
                title = "Bad Boys: Ride or Die",
                description = "Detectives Mike Lowrey and Marcus Burnett investigate corruption within the Miami Police Department.",
                genre = "Action",
                rating = 4.5,
                duration = "2h 5m",
                director = "Adil El Arbi",
                movieTimes = "1:30 PM, 4:30 PM, 7:30 PM",
                isActive = true
            ),
            Movie(
                title = "Dune: Part Two",
                description = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
                genre = "Sci-Fi",
                rating = 4.8,
                duration = "2h 46m",
                director = "Denis Villeneuve",
                movieTimes = "12:00 PM, 3:00 PM, 6:00 PM, 9:00 PM",
                isActive = true
            ),
            Movie(
                title = "Fly Me to the Moon",
                description = "A marketing executive is hired to help NASA sell the Apollo 11 moon landing to the American public.",
                genre = "Comedy",
                rating = 3.9,
                duration = "1h 52m",
                director = "Greg Berlanti",
                movieTimes = "2:15 PM, 5:15 PM, 8:15 PM",
                isActive = true
            ),
            Movie(
                title = "The Fall Guy",
                description = "A stuntman who gets fired from the biggest movie of his career and finds himself working on a set with his ex-girlfriend.",
                genre = "Action",
                rating = 4.1,
                duration = "2h 6m",
                director = "David Leitch",
                movieTimes = "1:45 PM, 4:45 PM, 7:45 PM",
                isActive = true
            )
        )
        
        defaultMovies.forEach { movie ->
            addMovie(movie)
        }
    }
}

