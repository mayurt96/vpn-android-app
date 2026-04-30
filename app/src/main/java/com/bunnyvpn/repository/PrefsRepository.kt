package com.bunnyvpn.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.bunnyvpn.model.UserProfile
import com.bunnyvpn.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bunnyvpn_prefs")

class PrefsRepository(private val context: Context) {

    companion object {
        val KEY_NAME = stringPreferencesKey("user_name")
        val KEY_EMAIL = stringPreferencesKey("user_email")
        val KEY_PHONE = stringPreferencesKey("user_phone")
        val KEY_PASSWORD = stringPreferencesKey("user_password")
        val KEY_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_AUTO_CONNECT = booleanPreferencesKey("auto_connect")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[KEY_NAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            phone = prefs[KEY_PHONE] ?: "",
            password = prefs[KEY_PASSWORD] ?: "",
            isLoggedIn = prefs[KEY_LOGGED_IN] ?: false
        )
    }

    val appThemeFlow: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_THEME]) {
            "LIGHT" -> AppTheme.LIGHT
            "NEON" -> AppTheme.NEON
            else -> AppTheme.DARK
        }
    }

    val autoConnectFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_CONNECT] ?: false
    }

    suspend fun saveUser(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME] = profile.name
            prefs[KEY_EMAIL] = profile.email
            prefs[KEY_PHONE] = profile.phone
            prefs[KEY_PASSWORD] = profile.password
            prefs[KEY_LOGGED_IN] = true
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN] = false
        }
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun saveAutoConnect(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_CONNECT] = enabled
        }
    }
}
