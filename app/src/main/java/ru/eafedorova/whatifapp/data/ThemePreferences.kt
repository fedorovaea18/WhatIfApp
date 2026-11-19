package ru.eafedorova.whatifapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")

        private val FREQUENCY_KEY = stringPreferencesKey("reminder_frequency")
        private val HOUR_KEY = intPreferencesKey("reminder_hour")
        private val MINUTE_KEY = intPreferencesKey("reminder_minute")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: "system"
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme
        }
    }

    suspend fun getReminderFrequency(): String {
        return context.dataStore.data.first()[FREQUENCY_KEY] ?: "daily"
    }

    suspend fun setReminderFrequency(frequency: String) {
        context.dataStore.edit { prefs ->
            prefs[FREQUENCY_KEY] = frequency
        }
    }

    suspend fun getReminderHour(): Int {
        return context.dataStore.data.first()[HOUR_KEY] ?: 19
    }

    suspend fun getReminderMinute(): Int {
        return context.dataStore.data.first()[MINUTE_KEY] ?: 0
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[HOUR_KEY] = hour
            prefs[MINUTE_KEY] = minute
        }
    }
}