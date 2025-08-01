package com.example.princecine.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.princecine.R
import com.google.android.material.button.MaterialButton
import android.widget.EditText
import android.widget.TextView

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

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
            // TODO: Handle login logic
        }

        tvSignUp.setOnClickListener {
            val intent = Intent(this, com.example.princecine.ui.SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
