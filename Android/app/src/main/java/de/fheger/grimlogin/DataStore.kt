package de.fheger.grimlogin

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
        val LOGIN_ID_KEY = stringPreferencesKey("login_id")
        val PASSWORD_KEY = stringPreferencesKey("password")
        val AUTOMATIC_LOGIN_ACTIVE_KEY = booleanPreferencesKey("automatic_login_active")
    }

    suspend fun saveCredentials(loginId: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[LOGIN_ID_KEY] = loginId
            preferences[PASSWORD_KEY] = password
        }
    }

    suspend fun getLoginId(): String? {
        return context.dataStore.data.map { it[LOGIN_ID_KEY] }.first()
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