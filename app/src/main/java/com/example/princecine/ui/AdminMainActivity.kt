package com.example.princecine.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.princecine.R
import com.example.princecine.ui.fragments.AdminHomeFragment
import com.example.princecine.ui.fragments.AdminProfileFragment
import com.example.princecine.ui.fragments.AdminSupportFragment
import com.example.princecine.ui.fragments.EarningsFragment
import com.example.princecine.ui.AddMovieDialog
import com.example.princecine.data.MovieDataManager
import com.example.princecine.data.FirebaseRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminMainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigationView: BottomNavigationView
    private var addMovieDialog: AddMovieDialog? = null
    private val repository = FirebaseRepository()
    
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
                R.id.nav_add_movie -> {
                    showAddMovieDialog()
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
    
    private fun showAddMovieDialog() {
        try {
            addMovieDialog = AddMovieDialog(this)
            addMovieDialog?.show { movie ->
                try {
                    // Movie is already added to Firebase in the dialog
                    // Just refresh the current fragment
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                    if (currentFragment is com.example.princecine.ui.fragments.AdminHomeFragment) {
                        currentFragment.refreshMovieList()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this, "Error refreshing movie list: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Error showing dialog: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    @Deprecated("Deprecated in favor of Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        addMovieDialog?.handleActivityResult(requestCode, resultCode, data)
    }
}

