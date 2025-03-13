package com.example.vibesshared.ui.ui.di // Correct package

import android.content.Context
import com.example.vibesshared.ui.ui.repository.PowerUpRepository
import com.example.vibesshared.ui.ui.repository.QuestGenerator
import com.example.vibesshared.ui.ui.repository.QuestRepository
import com.example.vibesshared.ui.ui.repository.StorageRepository
import com.example.vibesshared.ui.ui.repository.TriviaApiService
import com.example.vibesshared.ui.ui.repository.TriviaRepository
import com.example.vibesshared.ui.ui.repository.TriviaRetrofit
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTriviaApiService(retrofit: TriviaRetrofit): TriviaApiService {
        return retrofit.createApiService()
    }

    @Provides
    @Singleton
    fun provideTriviaRetrofit(): TriviaRetrofit {
        return TriviaRetrofit()
    }

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider() // This line stays
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        storage: FirebaseStorage,
        @ApplicationContext context: Context,
        dispatchers: DispatcherProvider
    ): StorageRepository = StorageRepository(storage, context, dispatchers)

    @Provides
    @Singleton
    fun provideQuestGenerator(): QuestGenerator {
        return QuestGenerator()
    }

    @Provides
    @Singleton
    fun provideQuestRepository(
        @ApplicationContext context: Context,
        questGenerator: QuestGenerator
    ): QuestRepository {
        return QuestRepository(context, questGenerator)
    }

    @Provides
    @Singleton
    fun providePowerUpRepository(
        @ApplicationContext context: Context,
        questRepository: QuestRepository
    ): PowerUpRepository {
        return PowerUpRepository(context, questRepository)
    }

    @Provides
    @Singleton
    fun provideTriviaRepository(
        triviaApiService: TriviaApiService,
        @ApplicationContext context: Context
    ): TriviaRepository {
        return TriviaRepository(triviaApiService, context)
    }
}
