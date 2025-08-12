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
            // TODO: Navigate to edit profile screen
            Toast.makeText(context, "Opening edit profile form", Toast.LENGTH_SHORT).show()
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
} 