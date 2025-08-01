package com.example.princecine.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.princecine.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Development login: username "c" with any password
            if (username == "c" && password.isNotEmpty()) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                
                // Navigate to CustomerMainActivity
                val intent = Intent(this, CustomerMainActivity::class.java)
                startActivity(intent)
                finish() // Close the login activity
            } else {
                Toast.makeText(this, "Invalid credentials! Use username 'c' and any password", Toast.LENGTH_LONG).show()
            }
        }

        tvSignUp.setOnClickListener {
            val intent = Intent(this, com.example.princecine.ui.RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
