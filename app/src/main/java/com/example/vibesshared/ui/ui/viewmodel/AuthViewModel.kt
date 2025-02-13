package com.example.vibesshared.ui.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.User
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.data.toUserProfile
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.enums.GreetingPreference
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.utils.Result
import com.example.vibesshared.ui.ui.viewmodel.AuthState.*
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}
 // <-- Verify this line VERY carefully



    @HiltViewModel
    class AuthViewModel @Inject constructor(
        private val repository: FirebaseRepository,
        val dispatchers: DispatcherProvider,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {

        private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
        val authState: StateFlow<AuthState> = _authState.asStateFlow()

        private val _isLoading = MutableStateFlow<Boolean>(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _greetingPreference =
            MutableStateFlow<GreetingPreference>(GreetingPreference.FIRST_NAME) // Default preference

        private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        private val GREETING_PREFERENCE_KEY = "greeting_preference"

        // Derived StateFlow for currentUserId
        val currentUserId: StateFlow<String?> = authState.map { state ->
            when (state) {
                is AuthState.Authenticated -> state.user?.uid
                else -> null
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        init {
            viewModelScope.launch {
                loadGreetingPreference()

                _isLoading.value = true
                Log.d("AuthViewModel", "Initializing AuthState...")
                val currentUser = repository.getCurrentUser()
                _authState.value = if (currentUser != null) {
                    Log.d("AuthViewModel", "Initial AuthState: Authenticated")
                    AuthState.Authenticated(currentUser)
                } else {
                    Log.d("AuthViewModel", "Initial AuthState: Unauthenticated")
                    AuthState.Unauthenticated
                }
                _isLoading.value = false
            }
        }

        // **Handle Authentication Exceptions (Private Helper Function)**
        private fun handleAuthException(e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                is FirebaseNetworkException -> "A network error occurred. Please check your internet connection."
                else -> "Authentication failed: ${e.message}" // More general message
            }
            _authState.value = AuthState.Error(errorMessage)
            Log.e("AuthViewModel", "Authentication error", e) // General log message
        }

        // **Login Function (Now Public and Correctly Placed)**
        fun login(email: String, password: String) {
            viewModelScope.launch(dispatchers.io) {
                _isLoading.value = true
                Log.d("AuthViewModel", "Login started for email: $email")
                try {
                    when (val result = repository.signIn(email, password)) {
                        is Result.Success -> {
                            val user = repository.getCurrentUser()
                            _authState.value = Authenticated(user)
                            Log.d("AuthViewModel", "Login successful, user UID: ${user?.uid}")
                        }

                        is Result.Failure -> {
                            val errorMessage = result.exception.message ?: "Login failed"
                            _authState.value = Error(errorMessage)
                            Log.e("AuthViewModel", "Login failed: $errorMessage")
                        }

                        is Result.Loading -> TODO()
                    }
                } catch (e: Exception) {
                    handleAuthException(e)
                } finally {
                    _isLoading.value = false
                    Log.d("AuthViewModel", "Login process finished")
                }
            }
        }

        // **Create Account Function (Now Public and Correctly Placed)**
        fun createAccount(
            email: String,
            password: String,
            userProfile: UserProfile,
            imageUri: Uri?,
        ) {
            viewModelScope.launch(dispatchers.io) {
                _isLoading.value = true
                Log.d("AuthViewModel", "Account creation started for email: $email")
                try {
                    when (val result = repository.signUp(email, password)) {
                        is Result.Success -> {
                            val user = repository.getCurrentUser()
                            if (user != null) {
                                val profilePictureUrl = imageUri?.let { uri ->
                                    when (val uploadResult =
                                        repository.updateProfilePicture(user.uid, uri)) {
                                        is Result.Success -> uploadResult.data
                                        is Result.Failure -> {
                                            Log.e(
                                                "AuthViewModel",
                                                "Profile picture upload failed: ${uploadResult.exception.message}"
                                            )
                                            null // Handle upload failure, maybe use default URL
                                        }

                                        is Result.Loading -> TODO()
                                    }
                                }
                                val newUser = User(
                                    userId = user.uid,
                                    email = email,
                                    profilePictureUrl = profilePictureUrl,
                                    userName = userProfile.userName,
                                    firstName = userProfile.firstName,
                                    lastName = userProfile.lastName
                                )
                                val profileResult =
                                    repository.updateUserProfile(newUser.toUserProfile())
                                if (profileResult is Result.Success) {
                                    _authState.value = Authenticated(user)
                                    Log.d(
                                        "AuthViewModel",
                                        "Account created and profile updated for user: ${user.uid}"
                                    )
                                } else {
                                    val errorMessage = (profileResult as? Result.Failure)?.exception?.message ?: "Failed to update user profile"
                                        _authState.value = Error(
                                            errorMessage

                                    )

                                    // Consider: Maybe sign out the partially created user here?
                                }
                            } else {
                                _authState.value =
                                    Error("Failed to get current user after signup")
                                Log.e("AuthViewModel", "Failed to get current user after signup")
                            }
                        }

                        is Result.Failure -> {
                            _authState.value = Error(
                                result.exception.message ?: "Account creation failed"
                            )
                            Log.e(
                                "AuthViewModel",
                                "Account creation failed: ${result.exception.message}"
                            )
                        }

                        is Result.Loading -> TODO()
                    }
                } catch (e: Exception) {
                    handleAuthException(e)
                } finally {
                    _isLoading.value = false
                    Log.d("AuthViewModel", "Account creation process finished")
                }
            }
        }

        // **Logout Function (Public)**
        fun logout() {
            viewModelScope.launch(dispatchers.io) {
                _isLoading.value = true
                try {
                    repository.signOut()
                    _authState.value = AuthState.Unauthenticated
                    Log.d("AuthViewModel", "Logout successful")
                } catch (e: Exception) {
                    _authState.value =
                        AuthState.Error("Logout Failed: ${e.message}") // Include error message
                    Log.e("AuthViewModel", "Logout error", e)
                } finally {
                    _isLoading.value = false
                    Log.d("AuthViewModel", "Logout process finished")
                }
            }
        }
        private fun loadGreetingPreference() {
            val preferenceName = sharedPreferences.getString(GREETING_PREFERENCE_KEY, GreetingPreference.FIRST_NAME.name)
            _greetingPreference.value = GreetingPreference.valueOf(preferenceName ?: GreetingPreference.FIRST_NAME.name)
        }
        // **Greeting Preference Functions (Public)**
        fun getGreetingPreference(): GreetingPreference {
            return _greetingPreference.value
        }

        private fun saveGreetingPreference(preference: GreetingPreference) {
            val editor = sharedPreferences.edit()
            editor.putString(GREETING_PREFERENCE_KEY, preference.name)
            editor.apply()
        }

        fun updateGreetingPreference(preference: GreetingPreference) {
            viewModelScope.launch {

                saveGreetingPreference(preference)

                _greetingPreference.value = preference
                Log.d("AuthViewModel", "Greeting preference updated to: $preference")
            }
        }
    }
