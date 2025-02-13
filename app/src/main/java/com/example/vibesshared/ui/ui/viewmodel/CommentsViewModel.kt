package com.example.vibesshared.ui.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.Comment
import com.example.vibesshared.ui.ui.di.DispatcherProvider
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val dispatchers: DispatcherProvider // Inject DispatcherProvider
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadComments(postId: String) {
        viewModelScope.launch(dispatchers.io) { // Use injected dispatcher
            _isLoading.value = true
            _error.value = null

            repository.getCommentsFlow(postId)
                .collect { comments -> // Collect the Flow directly
                    _comments.value = comments // Update the StateFlow
                    _isLoading.value = false // Set loading to false after receiving data
                }
        }
    }


    fun addComment(postId: String, text: String) {
        viewModelScope.launch(dispatchers.io) { // Use injected dispatcher
            try {
                _error.value = null
                val currentUser = repository.getCurrentUser()
                    ?: throw Exception("User not authenticated")

                // Fetch user data using getUser, handling Result
                when (val userResult = repository.getUserProfile(currentUser.uid)) {
                    is Result.Success -> {
                        val user = userResult.data
                        val comment = Comment(
                            postId = postId,
                            userId = currentUser.uid,
                            text = text,
                            userFirstName = user.firstName, // Use the retrieved user data
                            userLastName = user.lastName
                        )

                        // Add the comment, handling Result
                        // Add the comment, handling Result
                        when (val addResult = repository.addComment(postId, comment)) {
                            is Result.Success -> {
                                Log.d("CommentsVM", "Comment added successfully")
                                // No need to reload, getCommentsFlow uses a snapshot listener
                            }
                            is Result.Failure -> {
                                _error.value = "Failed to add comment: ${addResult.exception.message}"
                                Log.e("CommentsVM", "Add comment error", addResult.exception)
                            }

                            is Result.Loading -> TODO()
                        }
                    }
                    is Result.Failure -> {
                        _error.value = "Failed to fetch user data: ${userResult.exception.message}"
                        Log.e("CommentsVM", "Error fetching user", userResult.exception)
                    }

                    is Result.Loading -> TODO()
                }

            } catch (e: Exception) {
                _error.value = "Failed to add comment: ${e.message}"
                Log.e("CommentsVM", "Add comment error", e)
            }
        }
    }
}