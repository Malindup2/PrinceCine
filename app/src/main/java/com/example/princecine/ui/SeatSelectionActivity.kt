package com.example.princecine.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.princecine.R
import com.example.princecine.adapter.SeatAdapter
import com.example.princecine.data.FirebaseRepository
import com.example.princecine.model.Booking
import com.example.princecine.model.BookingStatus
import com.example.princecine.model.PaymentStatus
import com.example.princecine.model.Seat
import com.example.princecine.service.AuthService
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Timestamp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SeatSelectionActivity : AppCompatActivity() {
    
    companion object {
        private const val EXTRA_MOVIE_TITLE = "extra_movie_title"
        private const val EXTRA_MOVIE_DATE = "extra_movie_date"
        private const val EXTRA_MOVIE_TIME = "extra_movie_time"
        private const val EXTRA_MOVIE_ID = "extra_movie_id"
        private const val EXTRA_MOVIE_POSTER_BASE64 = "extra_movie_poster_base64"
        private const val SEAT_PRICE = 500.0 // LKR 500 per seat
        private const val PERMISSION_REQUEST_CODE = 100
        
        fun newIntent(context: Context, movieTitle: String, date: String, time: String): Intent {
            return Intent(context, SeatSelectionActivity::class.java).apply {
                putExtra(EXTRA_MOVIE_TITLE, movieTitle)
                putExtra(EXTRA_MOVIE_DATE, date)
                putExtra(EXTRA_MOVIE_TIME, time)
            }
        }
        
        fun newIntent(context: Context, movieId: String, movieTitle: String, moviePosterBase64: String?, date: String, time: String): Intent {
            return Intent(context, SeatSelectionActivity::class.java).apply {
                putExtra(EXTRA_MOVIE_ID, movieId)
                putExtra(EXTRA_MOVIE_TITLE, movieTitle)
                putExtra(EXTRA_MOVIE_POSTER_BASE64, moviePosterBase64)
                putExtra(EXTRA_MOVIE_DATE, date)
                putExtra(EXTRA_MOVIE_TIME, time)
            }
        }
    }
    
    private lateinit var ivBackButton: ImageView
    private lateinit var tvMovieTitle: MaterialTextView
    private lateinit var rvSeats: RecyclerView
    private lateinit var tvTotalPrice: MaterialTextView
    private lateinit var tvSelectedSeats: MaterialTextView
    private lateinit var btnContinue: MaterialButton
    
    private lateinit var seatAdapter: SeatAdapter
    private val selectedSeats = mutableSetOf<Seat>()
    private var totalPrice = 0.0
    
    // Firebase
    private lateinit var repository: FirebaseRepository
    private lateinit var authService: AuthService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_selection)
        
        // Initialize Firebase services
        repository = FirebaseRepository()
        authService = AuthService(this)
        
        initializeViews()
        setupClickListeners()
        loadMovieData()
        setupSeatGrid()
        updateBookingSummary() // Initialize the UI with empty state
    }
    
    private fun initializeViews() {
        ivBackButton = findViewById(R.id.ivBackButton)
        tvMovieTitle = findViewById(R.id.tvMovieTitle)
        rvSeats = findViewById(R.id.rvSeats)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        tvSelectedSeats = findViewById(R.id.tvSelectedSeats)
        btnContinue = findViewById(R.id.btnContinue)
    }
    
    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            finish()
        }
        
        btnContinue.setOnClickListener {
            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Please select at least one seat", Toast.LENGTH_SHORT).show()
            } else {
                purchaseTickets()
            }
        }
    }
    
    private fun purchaseTickets() {
        val currentUser = authService.getCurrentUser()
        if (currentUser == null) {
            Toast.makeText(this, "Please login to purchase tickets", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Please select at least one seat", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (checkStoragePermission()) {
            saveBookingToFirebase(currentUser.id)
        } else {
            requestStoragePermission()
        }
    }
    
    private fun saveBookingToFirebase(userId: String) {
        val movieId = intent.getStringExtra(EXTRA_MOVIE_ID) ?: ""
        val movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE) ?: "Unknown Movie"
        val moviePosterBase64 = intent.getStringExtra(EXTRA_MOVIE_POSTER_BASE64) ?: ""
        val showDate = intent.getStringExtra(EXTRA_MOVIE_DATE) ?: "Today"
        val showTime = intent.getStringExtra(EXTRA_MOVIE_TIME) ?: "12:30 PM"
        val bookingId = generateBookingId()
        
        val booking = Booking(
            bookingId = bookingId,
            userId = userId,
            movieId = movieId,
            movieTitle = movieTitle,
            moviePosterUrl = moviePosterBase64,
            showDate = showDate,
            showTime = showTime,
            seats = selectedSeats.map { it.seatNumber },
            totalAmount = totalPrice,
            status = BookingStatus.CONFIRMED,
            paymentMethod = "Cash", // You can make this dynamic
            paymentStatus = PaymentStatus.PAID,
            qrCodeUrl = "", // Can be added later if needed
            ticketPdfUrl = "", // Can be added later if needed
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        
        // Show loading
        btnContinue.isEnabled = false
        btnContinue.text = "Processing..."
        
        lifecycleScope.launch {
            try {
                Log.d("SeatSelection", "Creating booking: $booking")
                val result = repository.createBooking(booking)
                
                result.onSuccess { documentId ->
                    Log.d("SeatSelection", "Booking created successfully with ID: $documentId")
                    generateAndSaveTicket(bookingId)
                }.onFailure { error ->
                    Log.e("SeatSelection", "Failed to create booking", error)
                    Toast.makeText(this@SeatSelectionActivity, "Failed to save booking: ${error.message}", Toast.LENGTH_LONG).show()
                    btnContinue.isEnabled = true
                    btnContinue.text = "Continue - LKR ${String.format("%.0f", totalPrice)}"
                }
            } catch (e: Exception) {
                Log.e("SeatSelection", "Error creating booking", e)
                Toast.makeText(this@SeatSelectionActivity, "Error processing booking: ${e.message}", Toast.LENGTH_LONG).show()
                btnContinue.isEnabled = true
                btnContinue.text = "Continue - LKR ${String.format("%.0f", totalPrice)}"
            }
        }
    }
    
    private fun generateAndSaveTicket(bookingId: String) {
        try {
            // Create PDF ticket
            createTicketPDF(bookingId)
            
            // Show success message
            Toast.makeText(this, "Booking confirmed! Tickets purchased successfully!", Toast.LENGTH_LONG).show()
            
            // Show PDF download confirmation
            Toast.makeText(this, "Ticket PDF downloaded to your device.", Toast.LENGTH_LONG).show()
            
            // Reset button
            btnContinue.isEnabled = true
            btnContinue.text = "Continue"
            
            // Navigate back or to confirmation screen
            finish()
            
        } catch (e: Exception) {
            Log.e("SeatSelection", "Error generating ticket", e)
            Toast.makeText(this, "Booking saved but error generating ticket: ${e.message}", Toast.LENGTH_LONG).show()
            btnContinue.isEnabled = true
            btnContinue.text = "Continue - LKR ${String.format("%.0f", totalPrice)}"
        }
    }
    
    private fun checkStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10+ (API 29+), we can save to Downloads without special permission
            true
        } else {
            // For older versions, check WRITE_EXTERNAL_STORAGE permission
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            // Show explanation dialog for older Android versions
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs storage permission to save your ticket PDF to the Downloads folder. Please grant the permission to continue.")
                .setPositiveButton("Grant Permission") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(this, "Ticket purchase cancelled", Toast.LENGTH_SHORT).show()
                }
                .setCancelable(false)
                .show()
        } else {
            // For Android 10+, proceed with booking first
            val currentUser = authService.getCurrentUser()
            if (currentUser != null) {
                saveBookingToFirebase(currentUser.id)
            } else {
                Toast.makeText(this, "Please login to purchase tickets", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val currentUser = authService.getCurrentUser()
                    if (currentUser != null) {
                        saveBookingToFirebase(currentUser.id)
                    } else {
                        Toast.makeText(this, "Please login to purchase tickets", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Show dialog explaining how to enable permission manually
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("Storage permission is required to save your ticket PDF. You can enable it in Settings > Apps > PrinceCine > Permissions > Storage.")
                        .setPositiveButton("Go to Settings") { _, _ ->
                            // Open app settings
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = android.net.Uri.fromParts("package", packageName, null)
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            Toast.makeText(this, "Ticket purchase cancelled", Toast.LENGTH_SHORT).show()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }
    
    private fun generateBookingId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(1000)
        return "BK${timestamp}${random}"
    }
    
        private fun createTicketPDF(bookingId: String): File {
        val movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE) ?: "Unknown Movie"
        val date = intent.getStringExtra(EXTRA_MOVIE_DATE) ?: "Today"
        val time = intent.getStringExtra(EXTRA_MOVIE_TIME) ?: "12:30 PM"
        val seatNumbers = selectedSeats.joinToString(", ") { it.seatNumber }

        // Create PDF document
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Set clean white background
        val backgroundPaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.white)
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 842f, backgroundPaint)

        // Draw simple header with company name
        val headerPaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.red)
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 8- 210f, headerPaint)

        // Draw company title
        val titlePaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.white)
            textSize = 28f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("PRINCE CINEMA", 297.5f, 50f, titlePaint)

        // Draw main title below header
        val mainTitlePaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.black)
            textSize = 24f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("PriceCine Cinema", 297.5f, 130f, mainTitlePaint)

        // Draw simple border around the ticket
        val borderPaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.red)
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(40f, 150f, 555f, 750f, borderPaint)

        // Ticket Details Section
        val labelPaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, android.R.color.darker_gray)
            textSize = 14f
            isFakeBoldText = true
        }

        val detailPaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.black)
            textSize = 16f
        }

        var yPos = 180f
        val lineSpacing = 35f

        // Customer Name (placeholder)
        canvas.drawText("Customer:", 60f, yPos, labelPaint)
        canvas.drawText("John Doe", 200f, yPos, detailPaint)

        yPos += lineSpacing
        canvas.drawText("Movie:", 60f, yPos, labelPaint)
        canvas.drawText(movieTitle, 200f, yPos, detailPaint)

        yPos += lineSpacing
        canvas.drawText("Date:", 60f, yPos, labelPaint)
        canvas.drawText(date, 200f, yPos, detailPaint)

        yPos += lineSpacing
        canvas.drawText("Time:", 60f, yPos, labelPaint)
        canvas.drawText(time, 200f, yPos, detailPaint)

        yPos += lineSpacing
        canvas.drawText("Seats:", 60f, yPos, labelPaint)
        canvas.drawText(seatNumbers, 200f, yPos, detailPaint)

        yPos += lineSpacing
        canvas.drawText("Booking ID:", 60f, yPos, labelPaint)
        canvas.drawText(bookingId, 200f, yPos, detailPaint)

        yPos += lineSpacing
        canvas.drawText("Total:", 60f, yPos, labelPaint)
        val amountPaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.red)
            textSize = 18f
            isFakeBoldText = true
        }
        canvas.drawText("LKR ${String.format("%.0f", totalPrice)}", 200f, yPos, amountPaint)

        // QR Code Section (centered below all details)
        val qrCodeBitmap = generateQRCode(bookingId)
        if (qrCodeBitmap != null) {
            // Calculate center position for QR code
            val qrCodeSize = 150f
            val qrCodeX = (595f - qrCodeSize) / 2f  // Center horizontally
            val qrCodeY = yPos + 50f  // Position below the details
            
            // Draw QR code background
            val qrBackgroundPaint = android.graphics.Paint().apply {
                color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.white)
                style = android.graphics.Paint.Style.FILL
            }
            canvas.drawRect(qrCodeX - 10f, qrCodeY - 10f, qrCodeX + qrCodeSize + 10f, qrCodeY + qrCodeSize + 10f, qrBackgroundPaint)

            // Draw QR code border
            val qrBorderPaint = android.graphics.Paint().apply {
                color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.red)
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(qrCodeX - 10f, qrCodeY - 10f, qrCodeX + qrCodeSize + 10f, qrCodeY + qrCodeSize + 10f, qrBorderPaint)

            // Scale and draw QR code
            val scaledBitmap = Bitmap.createScaledBitmap(qrCodeBitmap, qrCodeSize.toInt(), qrCodeSize.toInt(), false)
            canvas.drawBitmap(scaledBitmap, qrCodeX, qrCodeY, null)

            // Draw QR code label (centered below QR code)
            val qrLabelPaint = android.graphics.Paint().apply {
                color = ContextCompat.getColor(this@SeatSelectionActivity, android.R.color.darker_gray)
                textSize = 12f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            canvas.drawText("Scan for verification", 297.5f, qrCodeY + qrCodeSize + 25f, qrLabelPaint)
        }

        // Footer section (positioned at the bottom)
        val footerY = 680f
        val footerPaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, android.R.color.darker_gray)
            textSize = 12f
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $currentDate", 297.5f, footerY, footerPaint)

        val thankYouPaint = android.graphics.Paint().apply {
            color = ContextCompat.getColor(this@SeatSelectionActivity, R.color.red)
            textSize = 14f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("Thank you for choosing PriceCine Cinema!", 297.5f, footerY + 25f, thankYouPaint)

        pdfDocument.finishPage(page)
        
        // Save PDF to Downloads folder
        val fileName = "ticket_${bookingId}.pdf"
        val file = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10+, use MediaStore API
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
            }
            
            val uri = contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { 
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        } else {
            // For older versions, use direct file access
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            file
        }
        
        pdfDocument.close()
        return file
    }
    

    
    private fun generateQRCode(content: String): Bitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200, hints)
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) ContextCompat.getColor(this, R.color.black) else ContextCompat.getColor(this, R.color.white))
                }
            }
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun loadMovieData() {
        val movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE) ?: "Unknown Movie"
        val date = intent.getStringExtra(EXTRA_MOVIE_DATE) ?: "Today"
        val time = intent.getStringExtra(EXTRA_MOVIE_TIME) ?: "12:30 PM"
        
        tvMovieTitle.text = "$movieTitle - $date, $time"
    }
    
    private fun setupSeatGrid() {
        val seats = generateSeatLayout()
        seatAdapter = SeatAdapter(seats) { seat ->
            toggleSeatSelection(seat)
        }
        
        rvSeats.layoutManager = GridLayoutManager(this, 10) // 10 columns for seat numbers 1-10
        rvSeats.adapter = seatAdapter
    }
    
    private fun generateSeatLayout(): List<Seat> {
        val seats = mutableListOf<Seat>()
        val rows = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")
        
        // Pre-defined taken seats based on the image
        val takenSeats = setOf(
            "A3", "A4", "A5", "B2", "B3", "B6", "B7", "B8", "B9", "B10",
            "C2", "C3", "C6", "C7", "C8", "C9", "C10", "D3", "D6", "D7", "D8", "D9", "D10",
            "E3", "E6", "E7", "E8", "E9", "E10", "F3", "F5", "F6", "F7", "F8", "F9", "F10",
            "G3", "G4", "G5", "G8", "H2", "H3", "H4", "H7", "H8", "I3", "I8", "J3", "J4", "J7", "J8", "K3", "K4", "K7", "K8"
        )
        
        rows.forEach { row ->
            for (col in 1..10) {
                val seatNumber = "$row$col"
                val isTaken = takenSeats.contains(seatNumber)
                val seat = Seat(
                    id = seatNumber,
                    seatNumber = seatNumber,
                    row = row,
                    column = col,
                    isTaken = isTaken,
                    isSelected = false
                )
                seats.add(seat)
            }
        }
        
        return seats
    }
    
    private fun toggleSeatSelection(seat: Seat) {
        if (seat.isTaken) {
            Toast.makeText(this, "This seat is already taken", Toast.LENGTH_SHORT).show()
            return
        }
        
        val seatIndex = seatAdapter.getSeats().indexOf(seat)
        
        if (selectedSeats.contains(seat)) {
            // Deselect seat
            selectedSeats.remove(seat)
            seat.isSelected = false
            totalPrice -= SEAT_PRICE
        } else {
            // Select seat
            selectedSeats.add(seat)
            seat.isSelected = true
            totalPrice += SEAT_PRICE
        }
        
        // Update the adapter to reflect the change
        if (seatIndex != -1) {
            seatAdapter.notifyItemChanged(seatIndex)
        }
        
        updateBookingSummary()
    }
    
    private fun updateBookingSummary() {
        // Format price as LKR with proper formatting
        tvTotalPrice.text = "LKR ${String.format("%.0f", totalPrice)}"
        
        // Update selected seats display
        tvSelectedSeats.text = if (selectedSeats.isEmpty()) {
            "No seats selected"
        } else {
            "Selected: ${selectedSeats.joinToString(", ") { it.seatNumber }}"
        }
        
        // Update continue button state
        btnContinue.isEnabled = selectedSeats.isNotEmpty()
        btnContinue.alpha = if (selectedSeats.isNotEmpty()) 1.0f else 0.5f
        
        // Update button text with current total
        if (selectedSeats.isNotEmpty()) {
            btnContinue.text = "Continue - LKR ${String.format("%.0f", totalPrice)}"
        } else {
            btnContinue.text = "Continue"
        }
    }
}
