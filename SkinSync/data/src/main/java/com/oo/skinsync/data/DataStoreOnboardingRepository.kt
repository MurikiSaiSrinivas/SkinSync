package com.oo.skinsync.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.oo.skinsync.domain.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreOnboardingRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : OnboardingRepository {

    private val key = booleanPreferencesKey("onboarding_seen")

    override val seen: Flow<Boolean> = dataStore.data.map { it[key] ?: false }

    override suspend fun markSeen() {
        dataStore.edit { it[key] = true }
    }
}
