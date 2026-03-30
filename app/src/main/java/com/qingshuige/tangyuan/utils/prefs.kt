package com.qingshuige.tangyuan.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tangyuan_prefs")

object PrefsManager {
    object Keys {
        const val APP_THEME_MODE = "app_theme_mode"
        const val APP_THEME_USER_OVERRIDDEN = "app_theme_user_overridden"
        const val CREATE_POST_DRAFT_PREFIX = "create_post_draft"
    }

    private lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        dataStore = context.dataStore
    }

    suspend fun getString(key: String, defaultValue: String = ""): String {
        val prefKey = stringPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }.first()
    }

    suspend fun putString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    suspend fun getInt(key: String, defaultValue: Int = 0): Int {
        val prefKey = intPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }.first()
    }

    suspend fun putInt(key: String, value: Int) {
        val prefKey = intPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val prefKey = booleanPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }.first()
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    suspend fun getLong(key: String, defaultValue: Long = 0L): Long {
        val prefKey = longPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }.first()
    }

    suspend fun putLong(key: String, value: Long) {
        val prefKey = longPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    suspend fun remove(key: String) {
        val stringKey = stringPreferencesKey(key)
        val intKey = intPreferencesKey(key)
        val booleanKey = booleanPreferencesKey(key)
        val longKey = longPreferencesKey(key)

        dataStore.edit { preferences ->
            preferences.remove(stringKey)
            preferences.remove(intKey)
            preferences.remove(booleanKey)
            preferences.remove(longKey)
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Flow 版本，用于在 Compose 中观察数据变化
    fun getStringFlow(key: String, defaultValue: String = ""): Flow<String> {
        val prefKey = stringPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }
    }

    fun getIntFlow(key: String, defaultValue: Int = 0): Flow<Int> {
        val prefKey = intPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }
    }

    fun getBooleanFlow(key: String, defaultValue: Boolean = false): Flow<Boolean> {
        val prefKey = booleanPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }
    }

    fun getLongFlow(key: String, defaultValue: Long = 0L): Flow<Long> {
        val prefKey = longPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[prefKey] ?: defaultValue
        }
    }

}
