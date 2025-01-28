package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.vibesshared.ui.ui.components.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    fun getUserDetails(userId: String) = flow {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userProfile = userDoc.toObject(UserProfile::class.java)
            emit(userProfile)
        } catch (e: Exception) {
            emit(null)
        }
    }
}
