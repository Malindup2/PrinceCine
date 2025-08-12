package com.example.princecine.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.princecine.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import android.text.TextWatcher
import android.util.Patterns

class ProfileFragment : Fragment() {
    
    private lateinit var tvWelcomeMessage: MaterialTextView
    private lateinit var tvFullName: MaterialTextView
    private lateinit var tvEmail: MaterialTextView
    private lateinit var tvPassword: MaterialTextView
    private lateinit var tvPhone: MaterialTextView
    private lateinit var tvDateOfBirth: MaterialTextView
    private lateinit var ivPasswordToggle: ImageView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnLogout: MaterialButton
    
    private var isPasswordVisible = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupClickListeners()
        loadUserData()
    }
    
    private fun initializeViews(view: View) {
        tvWelcomeMessage = view.findViewById(R.id.tvWelcomeMessage)
        tvFullName = view.findViewById(R.id.tvFullName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvPassword = view.findViewById(R.id.tvPassword)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvDateOfBirth = view.findViewById(R.id.tvDateOfBirth)
        ivPasswordToggle = view.findViewById(R.id.ivPasswordToggle)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)
    }
    
    private fun setupClickListeners() {
        // Password visibility toggle
        ivPasswordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
        
        // Edit Profile button
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
        
        // Logout button
        btnLogout.setOnClickListener {
            // TODO: Implement logout functionality
            Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        
        if (isPasswordVisible) {
            tvPassword.text = "password123"
            ivPasswordToggle.setImageResource(R.drawable.ic_eye_off)
        } else {
            tvPassword.text = "••••••••"
            ivPasswordToggle.setImageResource(R.drawable.ic_eye)
        }
    }
    
    private fun loadUserData() {
        // TODO: Load actual user data from SharedPreferences or database
        // For now, using sample data
        
        val userName = "John Doe"
        val userEmail = "john.doe@email.com"
        val userPhone = "+1 (555) 123-4567"
        val userDateOfBirth = "January 15, 1990"
        
        tvWelcomeMessage.text = "Welcome, $userName"
        tvFullName.text = userName
        tvEmail.text = userEmail
        tvPhone.text = userPhone
        tvDateOfBirth.text = userDateOfBirth
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null)
        
        val tilFullName = dialogView.findViewById<TextInputLayout>(R.id.tilFullName)

        val tilPassword = dialogView.findViewById<TextInputLayout>(R.id.tilPassword)
        val tilPhone = dialogView.findViewById<TextInputLayout>(R.id.tilPhone)
        val tilDateOfBirth = dialogView.findViewById<TextInputLayout>(R.id.tilDateOfBirth)
        
        val etFullName = dialogView.findViewById<TextInputEditText>(R.id.etFullName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etPassword)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.etPhone)
        val etDateOfBirth = dialogView.findViewById<TextInputEditText>(R.id.etDateOfBirth)
        
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)

        // Pre-fill the fields with current user data
        etFullName.setText(tvFullName.text.toString())
        etEmail.setText(tvEmail.text.toString())
        etPassword.setText("password123") // Current password
        etPhone.setText(tvPhone.text.toString())
        etDateOfBirth.setText(tvDateOfBirth.text.toString())

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set up button click listeners
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            if (validateInputs(tilFullName, tilPassword, tilPhone, tilDateOfBirth, etFullName, etPassword, etPhone, etDateOfBirth)) {
                saveProfileChanges(etFullName.text.toString(), etPhone.text.toString(), etDateOfBirth.text.toString())
                dialog.dismiss()
            }
        }

        // Set up date picker for Date of Birth
        etDateOfBirth.setOnClickListener {
            showDatePicker(etDateOfBirth)
        }

        // Set up text change listeners to clear errors when user types
        etFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tilFullName.error = null
            }
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tilPassword.error = null
            }
        })

        etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tilPhone.error = null
            }
        })

        dialog.show()
    }

    private fun validateInputs(
        tilFullName: TextInputLayout,
        tilPassword: TextInputLayout,
        tilPhone: TextInputLayout,
        tilDateOfBirth: TextInputLayout,
        etFullName: TextInputEditText,
        etPassword: TextInputEditText,
        etPhone: TextInputEditText,
        etDateOfBirth: TextInputEditText
    ): Boolean {
        var isValid = true

        // Validate full name
        if (etFullName.text.isNullOrBlank()) {
            tilFullName.error = "Full name is required"
            isValid = false
        } else if (etFullName.text.toString().length < 2) {
            tilFullName.error = "Full name must be at least 2 characters"
            isValid = false
        }

        // Validate password
        if (etPassword.text.isNullOrBlank()) {
            tilPassword.error = "Password is required"
            isValid = false
        } else if (etPassword.text.toString().length < 6) {
            tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        // Validate phone number
        if (etPhone.text.isNullOrBlank()) {
            tilPhone.error = "Phone number is required"
            isValid = false
        } else if (!isValidPhoneNumber(etPhone.text.toString())) {
            tilPhone.error = "Please enter a valid phone number"
            isValid = false
        }

        // Validate date of birth
        if (etDateOfBirth.text.isNullOrBlank()) {
            tilDateOfBirth.error = "Date of birth is required"
            isValid = false
        }

        return isValid
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Basic phone number validation - allows digits, spaces, parentheses, dashes, and plus sign
        val phonePattern = "^[+]?[0-9\\s\\(\\)\\-]{10,15}$"
        return phone.matches(phonePattern.toRegex())
    }

    private fun showDatePicker(etDateOfBirth: TextInputEditText) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Birth")
            .setSelection(Calendar.getInstance().timeInMillis)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            etDateOfBirth.setText(dateFormat.format(date))
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun saveProfileChanges(fullName: String, phone: String, dateOfBirth: String) {
        try {
            // Update the UI with new values
            tvFullName.text = fullName
            tvPhone.text = phone
            tvDateOfBirth.text = dateOfBirth
            tvWelcomeMessage.text = "Welcome, $fullName"
            
            // Update password display (masked)
            tvPassword.text = "••••••••"
            isPasswordVisible = false
            ivPasswordToggle.setImageResource(R.drawable.ic_eye)
            
            // Show success message
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_LONG).show()
            
            // TODO: Save to SharedPreferences or database
            // For now, just logging the changes
            android.util.Log.d("ProfileFragment", "Profile updated: $fullName, $phone, $dateOfBirth")
            
        } catch (e: Exception) {
            Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
} 