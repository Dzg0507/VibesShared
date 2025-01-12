package com.example.vibesshared.ui.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.components.UserProfile
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    private val auth = Firebase.auth

    init {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            _authState.value = if (currentUser != null) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                    is FirebaseAuthInvalidUserException -> "No account found for this email."
                    is FirebaseNetworkException -> "A network error occurred. Please check your internet connection."
                    else -> "Login failed: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
                Log.e("AuthViewModel", "Login error", e) // Log the exception for debugging
            }
        }
    }

    suspend fun createAccount(email: String, password: String, userProfile: UserProfile) {
        try {
            _authState.value = AuthState.Loading

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid

            if (userId != null) {
                val database = FirebaseDatabase.getInstance()
                val userRef = database.reference.child("users").child(userId)
                userRef.setValue(userProfile).await()
            }

            _authState.value = AuthState.Authenticated
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthUserCollisionException -> "An account with this email already exists."
                is FirebaseNetworkException -> "A network error occurred. Please check your internet connection."
                else -> "Account creation failed: ${e.message}"
            }
            _authState.value = AuthState.Error(errorMessage)
            Log.e("AuthViewModel", "Account creation error", e)
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Logout Failed")
                Log.e("AuthViewModel", "Logout error", e)
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}