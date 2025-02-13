// Dispatchers.kt (create a new file in a suitable package, e.g., ui/di)
package com.example.vibesshared.ui.ui.di // Or a better package like core.di

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

// Standard implementation for production

