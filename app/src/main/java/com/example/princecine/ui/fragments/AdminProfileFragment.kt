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
        loadAdminData()
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
            Toast.makeText(context, "Admin profile editing coming soon!", Toast.LENGTH_SHORT).show()
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
            tvPassword.text = "admin123"
            ivPasswordToggle.setImageResource(R.drawable.ic_eye_off)
        } else {
            tvPassword.text = "••••••••"
            ivPasswordToggle.setImageResource(R.drawable.ic_eye)
        }
    }
    
    private fun loadAdminData() {
        // TODO: Load actual admin data from SharedPreferences or database
        // For now, using sample admin data
        
        val adminName = "Admin User"
        val adminEmail = "admin@princecine.com"
        val adminPhone = "+1 (555) 999-8888"
        val adminDateOfBirth = "January 1, 1985"
        
        tvWelcomeMessage.text = "Welcome, $adminName (Admin)"
        tvFullName.text = adminName
        tvEmail.text = adminEmail
        tvPhone.text = adminPhone
        tvDateOfBirth.text = adminDateOfBirth
    }
}

