package com.example.vibesshared.ui.ui.utils

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Loading<T>(val data: T? = null) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}