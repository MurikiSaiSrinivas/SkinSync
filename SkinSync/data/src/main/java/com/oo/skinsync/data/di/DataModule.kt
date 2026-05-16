package com.oo.skinsync.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.oo.skinsync.data.AppSelfieStore
import com.oo.skinsync.data.DataStoreProfileRepository
import com.oo.skinsync.data.DeepLinkBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.oo.skinsync.data.DataStoreOnboardingRepository
import com.oo.skinsync.data.FirebaseAiSuggestionRepository
import com.oo.skinsync.data.FirestoreLooksRepository
import com.oo.skinsync.domain.LooksRepository
import com.oo.skinsync.domain.OnboardingRepository
import com.oo.skinsync.domain.ProfileRepository
import com.oo.skinsync.domain.SelfieStore
import com.oo.skinsync.domain.ShoppingLinkRepository
import com.oo.skinsync.domain.SuggestionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profile")

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        ctx.profileDataStore

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: DataStoreProfileRepository): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindShoppingLinkRepository(impl: DeepLinkBuilder): ShoppingLinkRepository

    @Binds
    @Singleton
    abstract fun bindSelfieStore(impl: AppSelfieStore): SelfieStore

    @Binds
    @Singleton
    abstract fun bindSuggestionRepository(impl: FirebaseAiSuggestionRepository): SuggestionRepository

    @Binds
    @Singleton
    abstract fun bindLooksRepository(impl: FirestoreLooksRepository): LooksRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: DataStoreOnboardingRepository): OnboardingRepository
}
