package ru.eafedorova.whatifapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
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

        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
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


    suspend fun getNotificationsEnabled(): Boolean {
        return context.dataStore.data.first()[NOTIFICATIONS_ENABLED_KEY] ?: false
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun getVibrationEnabled(): Boolean {
        return context.dataStore.data.first()[VIBRATION_ENABLED_KEY] ?: true
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[VIBRATION_ENABLED_KEY] = enabled
        }
    }
    val reminderFrequencyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FREQUENCY_KEY] ?: "daily"
    }

    val reminderHourFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[HOUR_KEY] ?: 19
    }

    val reminderMinuteFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[MINUTE_KEY] ?: 0
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED_KEY] ?: false
    }

    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[VIBRATION_ENABLED_KEY] ?: true
    }
}