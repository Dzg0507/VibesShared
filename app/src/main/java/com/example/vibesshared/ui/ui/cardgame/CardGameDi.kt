package com.example.vibesshared.ui.ui.cardgame // Adjust package name if needed

// Import DataStore classes
// Import Card Game classes (adjust paths if needed)
// REMOVED: import com.example.vibesshared.ui.ui.cardgame.CardGameNavigator // No longer needed
// Hilt imports
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Define a constant for the DataStore file name
private const val CARD_GAME_USER_PREFERENCES_NAME = "card_game_progress_prefs"

/**
 * Hilt module for providing singleton-scoped dependencies for the Card Game.
 */
@Module
@InstallIn(SingletonComponent::class)
object CardGameSingletonModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile(CARD_GAME_USER_PREFERENCES_NAME) }
        )
    }

    @Provides
    @Singleton
    fun provideCardGameProgressionRepository(
        dataStore: DataStore<Preferences> // Inject DataStore
    ): CardGameProgressionRepository {
        return CardGameProgressionRepository(dataStore) // Pass the injected dataStore here
    }

    // Inside CardGameDi.kt > CardGameSingletonModule
    @Provides
    @Singleton
    fun provideCardRepository( // Removed @Composable
        @ApplicationContext context: Context,
        cardLibraryRepository: CardLibraryRepository // Assuming CardLibraryRepository is provided elsewhere or in this module
    ): CardRepository {
        // Assuming CardRepository constructor just needs context and library repo
        return CardRepository(context, cardLibraryRepository)
    }

    @Provides
    @Singleton // SharedViewModel is typically a Singleton or ActivityRetainedScoped
    fun provideSharedGameViewModel(): SharedGameViewModel {
        return SharedGameViewModel()
    }

    // --- REMOVED: Provider for CardGameNavigator ---
    /*
    @Provides
    @Singleton // Or @ActivityRetainedScoped
    fun provideCardGameNavigator(): CardGameNavigator {
        return CardGameNavigator()
    }
    */

}

/**
 * Hilt module for providing ViewModel-scoped dependencies for the Card Game.
 */
@Module
@InstallIn(ViewModelComponent::class)
object CardGameViewModelModule {

    @Provides
    @ViewModelScoped // New instance per ViewModel
    fun provideBattleRepository(
        cardRepository: CardRepository // Example dependency from Singleton scope
    ): BattleRepository {
        // Ensure constructor takes dependencies if needed
        return BattleRepository(cardRepository)
    }

    @Provides
    @ViewModelScoped // New instance per ViewModel
    fun provideGamePhaseManager(
        // Inject dependencies required by GamePhaseManager
        battleRepository: BattleRepository, // Will get the ViewModelScoped instance
        cardRepository: CardRepository,     // Will get the Singleton instance
        cardGameProgressionRepository: CardGameProgressionRepository // Will get the Singleton instance
    ): GamePhaseManager {
        return GamePhaseManager(
            battleRepository = battleRepository,
            cardRepository = cardRepository,
            cardGameProgressionRepository = cardGameProgressionRepository // Use renamed repo
        )
    }
}