package com.example.vibesshared.ui.ui.di // Correct package

import com.example.vibesshared.ui.ui.repository.TriviaApiService
import com.example.vibesshared.ui.ui.repository.TriviaRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
        return DefaultDispatcherProvider() // This line STAYS
    }
}