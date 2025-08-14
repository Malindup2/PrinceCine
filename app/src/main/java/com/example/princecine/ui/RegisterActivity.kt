package com.example.princecine.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.princecine.R
import com.example.princecine.model.User
import com.example.princecine.model.UserRole
import com.example.princecine.service.AuthService
import com.example.princecine.ui.AdminMainActivity
import com.example.princecine.ui.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etDob: TextInputEditText
    private lateinit var spinnerRegisterAs: Spinner
    private lateinit var cbTerms: MaterialCheckBox
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLoginLink: MaterialTextView
    
    private lateinit var authService: AuthService
    // Removed coroutineScope as we'll use lifecycleScope instead
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        authService = AuthService(this)
        
        initializeViews()
        setupSpinner()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPhone = findViewById(R.id.etPhone)
        etDob = findViewById(R.id.etDob)
        spinnerRegisterAs = findViewById(R.id.spinnerRegisterAs)
        cbTerms = findViewById(R.id.cbTerms)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)
    }
    
    private fun setupSpinner() {
        val roles = resources.getStringArray(R.array.user_roles)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, roles)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerRegisterAs.adapter = adapter
    }
    
    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            android.util.Log.d("RegisterActivity", "Register button clicked")
            if (validateForm()) {
                android.util.Log.d("RegisterActivity", "Form validation passed, starting registration")
                performRegistration()
            } else {
                android.util.Log.d("RegisterActivity", "Form validation failed")
            }
        }
        
        // Setup date picker for Date of Birth
        etDob.setOnClickListener {
            showDatePicker()
        }
        
        // Setup login link
        tvLoginLink.setOnClickListener {
            finish() // Go back to previous activity (LoginActivity)
        }
    }
    
    private fun performRegistration() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val dob = etDob.text.toString().trim()
        val selectedRole = when (spinnerRegisterAs.selectedItem.toString()) {
            "Admin" -> UserRole.ADMIN
            else -> UserRole.CUSTOMER
        }
        
        val user = User(
            fullName = fullName,
            email = email,
            phone = phone,
            dateOfBirth = dob,
            role = selectedRole
        )
        
        // Show loading state
        btnRegister.isEnabled = false
        btnRegister.text = "Creating Account..."
        
        lifecycleScope.launch {
            try {
                val result = authService.register(user, password)
                
                withContext(Dispatchers.Main) {
                    result.onSuccess { newUser ->
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity(newUser.role)
                    }.onFailure { error ->
                        android.util.Log.e("RegisterActivity", "Registration failed", error)
                        val errorMessage = when {
                            error.message?.contains("CONFIGURATION_NOT_FOUND") == true -> 
                                "🔧 Firebase Setup Required!\n\n" +
                                "Please enable Authentication in Firebase Console:\n" +
                                "1. Go to console.firebase.google.com\n" +
                                "2. Select your project\n" +
                                "3. Go to Authentication\n" +
                                "4. Enable Email/Password sign-in"
                            error.message?.contains("PERMISSION_DENIED") == true || error.message?.contains("permission-denied") == true ->
                                "🔒 Database Permission Error!\n\n" +
                                "Please update Firestore Security Rules:\n" +
                                "1. Go to Firebase Console\n" +
                                "2. Go to Firestore Database\n" +
                                "3. Click 'Rules' tab\n" +
                                "4. Change to 'allow read, write: if true;'\n" +
                                "5. Click 'Publish'"
                            error.message?.contains("email-already-in-use") == true -> "This email is already registered. Please use a different email."
                            error.message?.contains("weak-password") == true -> "Password is too weak. Please choose a stronger password."
                            error.message?.contains("invalid-email") == true -> "Please enter a valid email address."
                            error.message?.contains("network") == true -> "Network error. Please check your internet connection."
                            else -> "Registration failed: ${error.message ?: "Unknown error"}"
                        }
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                        resetRegisterButton()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.util.Log.e("RegisterActivity", "Registration exception", e)
                    val errorMessage = "Internal error occurred: ${e.message ?: "Unknown error"}"
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                    resetRegisterButton()
                }
            }
        }
    }
    
    private fun navigateToMainActivity(role: UserRole) {
        val intent = when (role) {
            UserRole.ADMIN -> Intent(this, AdminMainActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
    
    private fun resetRegisterButton() {
        btnRegister.isEnabled = true
        btnRegister.text = "Register"
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etDob.setText(dateFormat.format(selectedDate.time))
            },
            year,
            month,
            day
        )
        
        // Set maximum date to today (user can't select future date)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun validateForm(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val dob = etDob.text.toString().trim()
        val selectedRole = spinnerRegisterAs.selectedItem.toString()
        
        // Basic validation - check if fields are not null/empty
        if (fullName.isEmpty()) {
            etFullName.error = "Full name is required"
            return false
        }
        
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }
        
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return false
        }
        
        if (phone.isEmpty()) {
            etPhone.error = "Phone number is required"
            return false
        }
        
        if (dob.isEmpty()) {
            etDob.error = "Date of birth is required"
            return false
        }
        
        if (!cbTerms.isChecked) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
}