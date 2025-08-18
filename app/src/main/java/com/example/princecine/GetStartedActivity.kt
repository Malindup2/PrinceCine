package com.example.princecine

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.princecine.service.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GetStartedActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)
        
        authService = AuthService(this)
        
        findViewById<Button>(R.id.btnGetStarted).setOnClickListener {
            // Clear any existing login data to ensure fresh start
            CoroutineScope(Dispatchers.Main).launch {
                authService.logout()
                startActivity(Intent(this@GetStartedActivity, com.example.princecine.ui.LoginActivity::class.java))
                finish()
            }
        }
    }
}