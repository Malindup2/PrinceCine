package com.example.princecine.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.princecine.R
import com.example.princecine.ui.fragments.AdminHomeFragment
import com.example.princecine.ui.fragments.AdminProfileFragment
import com.example.princecine.ui.fragments.AdminSupportFragment
import com.example.princecine.ui.fragments.EarningsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminMainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigationView: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)
        
        initializeViews()
        setupBottomNavigation()
        
        // Load default fragment (Admin Home)
        if (savedInstanceState == null) {
            loadFragment(AdminHomeFragment())
        }
    }
    
    private fun initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }
    
    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    loadFragment(AdminHomeFragment())
                    true
                }
                R.id.nav_earnings -> {
                    loadFragment(EarningsFragment())
                    true
                }
                R.id.nav_support -> {
                    loadFragment(AdminSupportFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(AdminProfileFragment())
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

