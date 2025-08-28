package com.example.princecine.service

import android.content.Context
import android.content.SharedPreferences
import com.example.princecine.data.FirebaseRepository
import com.example.princecine.model.User
import com.example.princecine.model.UserRole

class AuthService(private val context: Context) {
    
    private val repository = FirebaseRepository()
    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences("PrinceCine_Auth", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = repository.signIn(email, password)
            result.onSuccess { user ->
                saveUserToPrefs(user)
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(user: User, password: String): Result<User> {
        return try {
            val result = repository.signUp(user, password)
            result.onSuccess { newUser ->
                saveUserToPrefs(newUser)
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        repository.signOut()
        clearUserPrefs()
    }
    
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            repository.updatePassword(newPassword)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun getCurrentUser(): User? {
        if (!isLoggedIn()) return null
        
        return User(
            id = sharedPrefs.getString(KEY_USER_ID, "") ?: "",
            email = sharedPrefs.getString(KEY_USER_EMAIL, "") ?: "",
            fullName = sharedPrefs.getString(KEY_USER_NAME, "") ?: "",
            role = UserRole.valueOf(sharedPrefs.getString(KEY_USER_ROLE, UserRole.CUSTOMER.name) ?: UserRole.CUSTOMER.name)
        )
    }
    
    fun isAdmin(): Boolean {
        return getCurrentUser()?.role == UserRole.ADMIN
    }
    
    private fun saveUserToPrefs(user: User) {
        sharedPrefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.fullName)
            putString(KEY_USER_ROLE, user.role.name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    private fun clearUserPrefs() {
        sharedPrefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_ROLE)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
}
