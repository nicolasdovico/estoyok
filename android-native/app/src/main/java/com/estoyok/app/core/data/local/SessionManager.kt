package com.estoyok.app.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "estoyok_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_PHONE = stringPreferencesKey("user_phone")
    }

    val authTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    val userEmailFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    val userNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }

    suspend fun saveSession(token: String, name: String, email: String, phone: String?) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
            preferences[USER_NAME] = name
            preferences[USER_EMAIL] = email
            if (phone != null) {
                preferences[USER_PHONE] = phone
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
