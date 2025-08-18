package com.example.princecine.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.princecine.R
import com.example.princecine.model.UserRole
import com.example.princecine.service.AuthService
import com.example.princecine.ui.MainActivity
import com.example.princecine.ui.AdminMainActivity
import com.example.princecine.ui.CustomerMainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthService
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        authService = AuthService(this)
        
        // Check if user is already logged in
        if (authService.isLoggedIn()) {
            navigateToMainActivity()
            return
        }

        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)

        // Set 'Sign Up' part of the text to red
        val fullText = "Don't have an account? Sign Up"
        val signUpStart = fullText.indexOf("Sign Up")
        val signUpEnd = signUpStart + "Sign Up".length
        val spannable = android.text.SpannableString(fullText)
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(resources.getColor(R.color.red)),
            signUpStart,
            signUpEnd,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvSignUp.text = spannable

        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Email cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading state
            btnLogin.isEnabled = false
            btnLogin.text = "Logging in..."

            // Handle quick login for demo (backwards compatibility)
            if (email.lowercase() == "a" && password == "a") {
                // Quick admin login for demo
                Toast.makeText(this, "Admin login successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminMainActivity::class.java)
                startActivity(intent)
                finish()
                return@setOnClickListener
            } else if (email.lowercase() == "u" && password == "u") {
                // Quick user login for demo
                Toast.makeText(this, "User login successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, CustomerMainActivity::class.java)
                startActivity(intent)
                finish()
                return@setOnClickListener
            }
            
            // Firebase authentication
            performLogin(email, password)
        }

        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun performLogin(email: String, password: String) {
        coroutineScope.launch {
            try {
                val result = authService.login(email, password)
                
                withContext(Dispatchers.Main) {
                    result.onSuccess { user ->
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }.onFailure { error ->
                        Toast.makeText(this@LoginActivity, "Login failed: ${error.message}", Toast.LENGTH_SHORT).show()
                        resetLoginButton()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetLoginButton()
                }
            }
        }
    }
    
    private fun navigateToMainActivity() {
        val user = authService.getCurrentUser()
        val intent = when (user?.role) {
            UserRole.ADMIN -> Intent(this, AdminMainActivity::class.java)
            else -> Intent(this, CustomerMainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
    
    private fun resetLoginButton() {
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        btnLogin.isEnabled = true
        btnLogin.text = "Login"
    }
}
