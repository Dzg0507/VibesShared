package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        viewModelScope.launch {
            auth.addAuthStateListener { auth ->
                _authState.value = if (auth.currentUser != null) {
                    AuthState.Authenticated
                } else {
                    AuthState.Unauthenticated
                }
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
                // More specific error handling
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        _authState.value = AuthState.Error("Invalid email or password")
                    }
                    is FirebaseAuthInvalidUserException -> {
                        _authState.value = AuthState.Error("No account found for this email")
                    }
                    else -> {
                        _authState.value = AuthState.Error("Login failed: ${e.message}")
                    }
                }
            }
        }
    }

    fun createAccount(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
                // Handle the exception appropriately (e.g., display an error message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }
}