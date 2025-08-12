package com.example.princecine.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.princecine.R
import com.example.princecine.ui.fragments.HomeFragment
import com.example.princecine.ui.fragments.MyTicketsFragment
import com.example.princecine.ui.fragments.ProfileFragment
import com.example.princecine.ui.fragments.SupportFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class CustomerMainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigationView: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_main)
        
        initializeViews()
        setupBottomNavigation()
        
        // Set default fragment
        loadFragment(HomeFragment())
    }
    
    private fun initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }
    
    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_tickets -> {
                    loadFragment(MyTicketsFragment())
                    true
                }
                R.id.nav_support -> {
                    loadFragment(SupportFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
} 