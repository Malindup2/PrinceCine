package com.example.princecine

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Navigate to GetStartedActivity after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, GetStartedActivity::class.java))
            finish()
        }, 2000) // 2 seconds delay
    }
}