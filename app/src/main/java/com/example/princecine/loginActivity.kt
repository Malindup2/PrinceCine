package com.example.princecine

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class loginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        findViewById<Button>(R.id.ivLogin).setOnClickListener {
            startActivity(Intent(this, com.example.princecine.ui.LoginActivity::class.java))
            finish()
        }
    }
}