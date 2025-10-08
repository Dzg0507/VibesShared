/*
package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.DataManager
import com.example.vibesshared.ui.ui.data.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val dataManager: DataManager
) : ViewModel() {

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    init {
        fetchFriends()
    }

    private fun fetchFriends() {
        viewModelScope.launch {
            try {
                dataManager.fetchFriends()
                    .collectLatest { friendsList ->
                        _friends.value = friendsList
                    }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

*/
