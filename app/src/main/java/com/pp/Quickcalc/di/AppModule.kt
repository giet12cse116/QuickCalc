package com.pp.Quickcalc.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.pp.Quickcalc.audio.FeedbackManager
import com.pp.Quickcalc.data.GameRepository
import com.pp.Quickcalc.data.GameRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.google.firebase.analytics.FirebaseAnalytics

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "numflow_settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        impl: GameRepositoryImpl
    ): GameRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideFeedbackManager(
        @ApplicationContext context: Context
    ): FeedbackManager? {
        return try {
            FeedbackManager(context).apply { init() }
        } catch (e: Exception) {
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics? {
        return try {
            FirebaseAnalytics.getInstance(context)
        } catch (e: Exception) {
            null
        }
    }
}
