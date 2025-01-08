// AuthViewModel.kt
package com.example.vibesshared.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginResult: StateFlow<LoginResult> = _loginResult.asStateFlow()

    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginResult.value = LoginResult.Success
                } else {
                    _loginResult.value = LoginResult.Failure(task.exception?.message ?: "Login failed")
                    Log.w("AuthViewModel", "signInWithEmailAndPassword failed", task.exception)
                }
            }
    }

    sealed class LoginResult {
        object Idle : LoginResult()
        object Success : LoginResult()
        data class Failure(val message: String) : LoginResult()
    }
}