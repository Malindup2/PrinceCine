package com.example.princecine.data

import android.net.Uri
import android.util.Log
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
    
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.updatePassword(newPassword).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user is currently signed in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    // Real-time movie listeners
    fun getMoviesRealtime(callback: (List<Movie>) -> Unit): () -> Unit {
        android.util.Log.d("FirebaseRepository", "Setting up real-time movies listener")
        val listenerRegistration = db.collection("movies")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirebaseRepository", "Error listening for movies", error)
                    callback(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot == null) {
                    android.util.Log.w("FirebaseRepository", "Movies snapshot is null")
                    callback(emptyList())
                    return@addSnapshotListener
                }
                
                android.util.Log.d("FirebaseRepository", "Received movies snapshot with ${snapshot.documents.size} documents")
                
                val movies = snapshot.documents.mapNotNull { doc ->
                    try {
                        val movie = doc.toObject(Movie::class.java)?.copy(id = doc.id)
                        android.util.Log.d("FirebaseRepository", "Movie loaded: ${movie?.title} (ID: ${doc.id})")
                        movie
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseRepository", "Error parsing movie: ${doc.id}", e)
                        null
                    }
                }
                
                android.util.Log.d("FirebaseRepository", "Total movies loaded: ${movies.size}, returning to callback")
                callback(movies)
            }
        
        // Return unsubscribe function
        return { 
            android.util.Log.d("FirebaseRepository", "Unsubscribing movies listener")
            listenerRegistration.remove() 
        }
    }
    
    fun getMovieEarningsRealtime(callback: (List<MovieEarnings>) -> Unit): () -> Unit {
        val listenerRegistration = db.collection("bookings")
            .whereEqualTo("status", "CONFIRMED")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirebaseRepository", "Error listening for earnings", error)
                    return@addSnapshotListener
                }
                
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                // Group bookings by movieId and calculate earnings
                val earningsMap = mutableMapOf<String, MovieEarnings>()
                
                bookings.forEach { booking ->
                    val movieId = booking.movieId
                    val existing = earningsMap[movieId]
                    
                    if (existing != null) {
                        earningsMap[movieId] = existing.copy(
                            ticketsSold = existing.ticketsSold + booking.seats.size,
                            totalEarnings = existing.totalEarnings + booking.totalAmount
                        )
                    } else {
                        earningsMap[movieId] = MovieEarnings(
                            movieId = movieId,
                            movieTitle = booking.movieTitle,
                            ticketsSold = booking.seats.size,
                            totalEarnings = booking.totalAmount
                        )
                    }
                }
                
                // Now fetch complete movie details including poster data
                if (earningsMap.isNotEmpty()) {
                    fetchMovieDetailsForEarnings(earningsMap.values.toList()) { updatedEarnings ->
                        callback(updatedEarnings)
                    }
                } else {
                    callback(emptyList())
                }
            }
        
        return { listenerRegistration.remove() }
    }
    
    private fun fetchMovieDetailsForEarnings(
        basicEarnings: List<MovieEarnings>, 
        callback: (List<MovieEarnings>) -> Unit
    ) {
        // Get all unique movie IDs
        val movieIds = basicEarnings.map { it.movieId }.distinct()
        
        if (movieIds.isEmpty()) {
            callback(basicEarnings)
            return
        }
        
        // Fetch movie details for all movie IDs
        db.collection("movies")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), movieIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val movieDetailsMap = snapshot.documents.associate { doc ->
                    val movie = doc.toObject(Movie::class.java)?.copy(id = doc.id)
                    doc.id to movie
                }
                
                // Update earnings with complete movie details
                val updatedEarnings = basicEarnings.map { earnings ->
                    val movieDetails = movieDetailsMap[earnings.movieId]
                    earnings.copy(
                        movieTitle = movieDetails?.title ?: earnings.movieTitle,
                        posterBase64 = movieDetails?.posterBase64,
                        posterUrl = movieDetails?.posterUrl ?: ""
                    )
                }
                
                callback(updatedEarnings)
            }
            .addOnFailureListener { error ->
                android.util.Log.e("FirebaseRepository", "Error fetching movie details for earnings", error)
                // Return basic earnings if movie details fetch fails
                callback(basicEarnings)
            }
    }
    
    // Real-time user listener
    fun getUserRealtime(userId: String, callback: (User?) -> Unit): () -> Unit {
        val listenerRegistration = db.collection("users")
            .document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    android.util.Log.e("FirebaseRepository", "Error listening for user", error)
                    callback(null)
                    return@addSnapshotListener
                }
                
                val user = document?.toObject(User::class.java)?.copy(id = document.id)
                callback(user)
            }
        
        return { listenerRegistration.remove() }
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
            Log.d("FirebaseRepository", "Starting to add movie: ${movie.title}")
            
            // Check if the poster Base64 is too large
            if (!movie.posterBase64.isNullOrEmpty() && movie.posterBase64.length > 800000) {
                Log.e("FirebaseRepository", "Movie poster is too large: ${movie.posterBase64.length} characters")
                return Result.failure(Exception("Movie poster image is too large. Please select a smaller image or reduce quality."))
            }
            
            val movieData = movie.copy(
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            Log.d("FirebaseRepository", "Movie data prepared: ${movieData.title}, isActive: ${movieData.isActive}")
            Log.d("FirebaseRepository", "Poster size: ${movieData.posterBase64?.length ?: 0} characters")
            
            val docRef = db.collection("movies").add(movieData).await()
            Log.d("FirebaseRepository", "Movie added successfully with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Failed to add movie: ${e.message}", e)
            
            // Provide more specific error message for image size issues
            val errorMessage = if (e.message?.contains("longer than") == true) {
                "Movie poster image is too large for storage. Please select a smaller image."
            } else {
                e.message ?: "Unknown error occurred"
            }
            
            Result.failure(Exception(errorMessage))
        }
    }
    
    suspend fun updateMovie(movie: Movie): Result<Unit> {
        return try {
            // Check if the poster Base64 is too large
            if (!movie.posterBase64.isNullOrEmpty() && movie.posterBase64.length > 800000) {
                Log.e("FirebaseRepository", "Movie poster is too large: ${movie.posterBase64.length} characters")
                return Result.failure(Exception("Movie poster image is too large. Please select a smaller image or reduce quality."))
            }
            
            val movieData = movie.copy(updatedAt = Timestamp.now())
            Log.d("FirebaseRepository", "Updating movie: ${movieData.title}, poster size: ${movieData.posterBase64?.length ?: 0} characters")
            
            db.collection("movies").document(movie.id).set(movieData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Failed to update movie: ${e.message}", e)
            
            // Provide more specific error message for image size issues
            val errorMessage = if (e.message?.contains("longer than") == true) {
                "Movie poster image is too large for storage. Please select a smaller image."
            } else {
                e.message ?: "Unknown error occurred"
            }
            
            Result.failure(Exception(errorMessage))
        }
    }
    
    suspend fun deleteMovie(movieId: String): Result<Unit> {
        return try {
            Log.d("FirebaseRepository", "Attempting to permanently delete movie with ID: $movieId")
            
            // First, get the movie document to verify it exists
            val docSnapshot = db.collection("movies").document(movieId).get().await()
            if (!docSnapshot.exists()) {
                Log.e("FirebaseRepository", "Movie document does not exist: $movieId")
                return Result.failure(Exception("Movie not found"))
            }
            
            Log.d("FirebaseRepository", "Movie found, permanently deleting: ${docSnapshot.data}")
            
            // Hard delete - completely remove the document from Firebase
            db.collection("movies").document(movieId)
                .delete()
                .await()
                
            Log.d("FirebaseRepository", "Successfully PERMANENTLY deleted movie $movieId from Firebase")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error permanently deleting movie $movieId", e)
            Result.failure(e)
        }
    }
    
    // Helper method to update existing movies with isActive field
    suspend fun updateExistingMoviesWithActiveField(): Result<Unit> {
        return try {
            Log.d("FirebaseRepository", "Updating existing movies with isActive field")
            val snapshot = db.collection("movies").get().await()
            
            snapshot.documents.forEach { doc ->
                if (!doc.contains("isActive")) {
                    Log.d("FirebaseRepository", "Adding isActive=true to movie: ${doc.id}")
                    doc.reference.update("isActive", true)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Failed to update existing movies", e)
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
                            movieId = movieId, // Keep as string
                            movieTitle = it.title,
                            posterBase64 = it.posterBase64,
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
                // Add some sample movies only for demonstration
                android.util.Log.d("FirebaseRepository", "Database is empty, no default data will be added. Admin can add movies through the app.")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error checking database", e)
        }
    }
}

