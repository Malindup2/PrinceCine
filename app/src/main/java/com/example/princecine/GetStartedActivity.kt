package com.example.princecine

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class GetStartedActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)
        findViewById<Button>(R.id.btnGetStarted).setOnClickListener {
            startActivity(Intent(this, com.example.princecine.ui.LoginActivity::class.java))
            finish()
        }
    }
}