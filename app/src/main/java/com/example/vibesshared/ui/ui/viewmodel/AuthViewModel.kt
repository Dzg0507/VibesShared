package com.example.vibesshared.ui.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.components.UserProfile
import com.example.vibesshared.ui.ui.navigation.Screen
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class NavigationEvent {
    data class NavigateToRoute(
        val route: String,
        val popUpToRoute: String? = null,
        val inclusive: Boolean = false
    ) : NavigationEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: FirebaseAuth) : ViewModel() {

    private val db = Firebase.firestore

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUser = auth.currentUser
            _authState.value = if (currentUser != null) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
                _navigationEvent.emit(
                    NavigationEvent.NavigateToRoute(
                        Screen.Home.route,
                        Screen.Login.route,
                        true
                    )
                )
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                    is FirebaseAuthInvalidUserException -> "No account found for this email."
                    is FirebaseNetworkException -> "A network error occurred. Please check your internet connection."
                    else -> "Login failed: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
                Log.e("AuthViewModel", "Login error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAccount(email: String, password: String, userProfile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _authState.value = AuthState.Loading

                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user

                if (user != null) {
                    val updatedProfile = userProfile.copy(userId = user.uid)
                    db.collection("users").document(user.uid).set(updatedProfile).await()

                    _authState.value = AuthState.Authenticated
                    _navigationEvent.emit(
                        NavigationEvent.NavigateToRoute(
                            Screen.Home.route,
                            Screen.Login.route,
                            true
                        )
                    )
                } else {
                    _authState.value = AuthState.Error("Failed to create user")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    is FirebaseNetworkException -> "A network error occurred. Please check your internet connection."
                    else -> "Account creation failed: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
                Log.e("AuthViewModel", "Account creation error", e)
            } finally {
                _isLoading.value = false
            }
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
