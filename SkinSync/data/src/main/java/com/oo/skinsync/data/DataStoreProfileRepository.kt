package com.oo.skinsync.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.oo.skinsync.domain.Profile
import com.oo.skinsync.domain.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Offline-first profile store backed by Preferences DataStore. */
@Singleton
class DataStoreProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ProfileRepository {

    override val profile: Flow<Profile> = dataStore.data.map { prefs ->
        ProfileMapping.fromMap(prefs.asMap().mapKeys { it.key.name }.mapValues { it.value as? String })
    }

    override suspend fun save(profile: Profile) {
        val map = ProfileMapping.toMap(profile)
        dataStore.edit { prefs ->
            prefs.clear()
            map.forEach { (k, v) -> prefs[stringPreferencesKey(k)] = v }
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
