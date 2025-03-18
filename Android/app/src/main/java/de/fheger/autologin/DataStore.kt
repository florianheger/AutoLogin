package de.fheger.autologin

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "login_id")

class DataStore(private val context: Context) {
    companion object {
        val EMAIL_KEY = stringPreferencesKey("email")
        val PASSWORD_KEY = stringPreferencesKey("password")
        val AUTOMATIC_LOGIN_ACTIVE_KEY = booleanPreferencesKey("automatic_login_active")
    }

    suspend fun saveCredentials(email: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
            preferences[PASSWORD_KEY] = password
        }
    }

    suspend fun getEmail(): String? {
        return context.dataStore.data.map { it[EMAIL_KEY] }.first()
    }

    suspend fun getPassword(): String? {
        return context.dataStore.data.map { it[PASSWORD_KEY] }.first()
    }

    suspend fun getAutomaticLoginActive(): Boolean {
        return context.dataStore.data.map { it[AUTOMATIC_LOGIN_ACTIVE_KEY] }.first() ?: false
    }

    suspend fun setAutomaticLoginActive(isAutomaticLoginActive: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTOMATIC_LOGIN_ACTIVE_KEY] = isAutomaticLoginActive
        }
    }
}