package com.example.princecine

import android.app.Application
import com.example.princecine.data.FirebaseRepository
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PrinceCineApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize default data (this will only run once when the database is empty)
        GlobalScope.launch {
            try {
                val repository = FirebaseRepository()
                repository.initializeDefaultData()
            } catch (e: Exception) {
                android.util.Log.e("PrinceCineApplication", "Error initializing default data", e)
            }
        }
    }
}
