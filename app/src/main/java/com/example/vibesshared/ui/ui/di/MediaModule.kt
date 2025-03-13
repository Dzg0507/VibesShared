package com.example.vibesshared.ui.ui.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Transformer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class) // Or SingletonComponent if you want a single instance for the whole app
object MediaModule {

    @Provides
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    @UnstableApi
    @Provides
    fun provideTransformer(@ApplicationContext context: Context): Transformer {
        return Transformer.Builder(context).build() // Simplified Transformer Builder
    }
}