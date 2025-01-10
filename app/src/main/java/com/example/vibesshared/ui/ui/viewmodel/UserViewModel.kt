package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.components.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("users")
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private fun getUserReference(): DatabaseReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return userRef.child(uid)
    }

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            val userRef = getUserReference() ?: return@launch
            userRef.get().addOnSuccessListener { snapshot ->
                val userProfile = snapshot.getValue(UserProfile::class.java)
                _userProfile.value = userProfile
            }.addOnFailureListener {
                _userProfile.value = null
            }
        }
    }
}