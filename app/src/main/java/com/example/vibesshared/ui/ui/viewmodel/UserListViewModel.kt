/*
package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.components.UserProfile
import com.example.vibesshared.ui.ui.data.DataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val dataManager: DataManager
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                dataManager.fetchUsers().collectLatest {
                    _users.value = it
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                dataManager.searchUsers(query).collectLatest {
                    _users.value = it
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
*/