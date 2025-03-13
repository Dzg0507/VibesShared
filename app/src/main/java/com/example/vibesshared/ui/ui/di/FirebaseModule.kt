package com.example.vibesshared.ui.ui.di

import android.content.Context
import android.content.SharedPreferences
import com.example.vibesshared.ui.ui.repository.BadgeRepository
import com.example.vibesshared.ui.ui.repository.FirebaseRepository
import com.example.vibesshared.ui.ui.repository.StorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth // Used indirectly via FirebaseRepository in AuthViewModel and ChatsViewModel

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore // Used indirectly via FirebaseRepository and BadgeRepository

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage // Used indirectly via FirebaseRepository and StorageRepository

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE) // Used in AuthViewModel for SharedPreferences
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage,
        dispatchers: DispatcherProvider,
        storageRepository: StorageRepository // Ensure this is injected and used
    ): FirebaseRepository {
        return FirebaseRepository(context, firestore, auth, storage, dispatchers, storageRepository) // Used in AuthViewModel and ChatsViewModel
    }

    @Provides
    @Singleton
    fun provideBadgeRepository(
        firestore: FirebaseFirestore,
        dispatcherProvider: DispatcherProvider
    ): BadgeRepository {
        return BadgeRepository(firestore, dispatcherProvider) // Used in MainActivity via BadgeInitializer
    }
}