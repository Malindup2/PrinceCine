package com.example.princecine.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.princecine.R
import com.example.princecine.data.FirebaseRepository
import com.example.princecine.service.AuthService
import com.example.princecine.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import android.text.TextWatcher
import kotlinx.coroutines.launch

class AdminProfileFragment : Fragment() {
    
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
    private lateinit var authService: AuthService
    private lateinit var repository: FirebaseRepository
    private var currentUser: User? = null
    private var unsubscribeUserListener: (() -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authService = AuthService(requireContext())
        repository = FirebaseRepository()
        
        initializeViews(view)
        setupClickListeners()
        setupRealtimeUserListener()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Unsubscribe from real-time listener
        unsubscribeUserListener?.invoke()
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
            performLogout()
        }
    }
    
    private fun performLogout() {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout_confirmation, null)
        
        // Create dialog with custom style
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        // Set dialog window properties for rounded corners
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Handle button clicks
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnLogout = dialogView.findViewById<MaterialButton>(R.id.btnLogout)
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnLogout.setOnClickListener {
            dialog.dismiss()
            lifecycleScope.launch {
                try {
                    authService.logout()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to LoginActivity
                    val intent = android.content.Intent(requireContext(), com.example.princecine.ui.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                } catch (e: Exception) {
                    Log.e("AdminProfileFragment", "Logout failed", e)
                    Toast.makeText(context, "Logout failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        dialog.show()
    }
    
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        
        if (isPasswordVisible) {
            // Show masked password instead of hardcoded value
            tvPassword.text = "••••••••••"
            ivPasswordToggle.setImageResource(R.drawable.ic_eye_off)
        } else {
            tvPassword.text = "••••••••"
            ivPasswordToggle.setImageResource(R.drawable.ic_eye)
        }
    }
    
    private fun setupRealtimeUserListener() {
        val user = authService.getCurrentUser()
        if (user != null) {
            unsubscribeUserListener = repository.getUserRealtime(user.id) { updatedUser ->
                currentUser = updatedUser
                showLoading(false)
                if (updatedUser != null) {
                    updateUIWithUserData(updatedUser)
                } else {
                    Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            showLoading(false)
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showLoading(show: Boolean) {
        // Simple loading state - just show/hide profile content
        val views = listOf(tvWelcomeMessage, tvFullName, tvEmail, tvPassword, tvPhone, tvDateOfBirth, btnEditProfile, btnLogout)
        views.forEach { it.visibility = if (show) View.GONE else View.VISIBLE }
    }
    
    private fun updateUIWithUserData(user: User) {
        tvWelcomeMessage.text = "Welcome, ${user.fullName} (Admin)"
        tvFullName.text = user.fullName
        tvEmail.text = user.email
        tvPhone.text = user.phone ?: "Not provided"
        tvDateOfBirth.text = user.dateOfBirth ?: "Not provided"
        
        // Hide password for security
        tvPassword.text = "••••••••"
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null)
        
        val tilFullName = dialogView.findViewById<TextInputLayout>(R.id.tilFullName)
        val tilEmail = dialogView.findViewById<TextInputLayout>(R.id.tilEmail)
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
        currentUser?.let { user ->
            etFullName.setText(user.fullName)
            etEmail.setText(user.email)
            etPassword.setText("") // Leave password empty for security
            etPhone.setText(user.phone)
            etDateOfBirth.setText(user.dateOfBirth)
        }

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
                val newPassword = if (etPassword.text.toString().isNotBlank()) etPassword.text.toString() else null
                saveProfileChanges(etFullName.text.toString(), etPhone.text.toString(), etDateOfBirth.text.toString(), newPassword)
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

        // Validate password (optional - only if user wants to change it)
        if (etPassword.text.isNullOrBlank()) {
            // Password is optional - user can leave it blank to keep current password
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

    private fun saveProfileChanges(fullName: String, phone: String, dateOfBirth: String, newPassword: String?) {
        lifecycleScope.launch {
            try {
                currentUser?.let { user ->
                    val updatedUser = user.copy(
                        fullName = fullName,
                        phone = phone,
                        dateOfBirth = dateOfBirth
                    )
                    
                    val result = repository.updateUser(updatedUser)
                    result.onSuccess {
                        // Update password if provided
                        if (newPassword != null) {
                            try {
                                authService.updatePassword(newPassword)
                                Toast.makeText(requireContext(), "Admin profile and password updated successfully!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("AdminProfileFragment", "Failed to update password", e)
                                Toast.makeText(requireContext(), "Admin profile updated but password update failed", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Admin profile updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                        
                        // Update local user data
                        currentUser = updatedUser
                        
                        // Update the UI with new values
                        tvFullName.text = fullName
                        tvPhone.text = phone
                        tvDateOfBirth.text = dateOfBirth
                        tvWelcomeMessage.text = "Welcome, $fullName (Admin)"
                        
                        // Update password display (masked)
                        tvPassword.text = "••••••••"
                        isPasswordVisible = false
                        ivPasswordToggle.setImageResource(R.drawable.ic_eye)
                        
                    }.onFailure { error ->
                        Toast.makeText(requireContext(), "Failed to update admin profile: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating admin profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

