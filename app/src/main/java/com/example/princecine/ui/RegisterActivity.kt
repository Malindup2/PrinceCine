package com.example.princecine.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.princecine.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
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
            if (validateForm()) {
                // TODO: Handle registration logic
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
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