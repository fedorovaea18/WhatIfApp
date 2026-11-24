package ru.eafedorova.whatifapp

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.data.ThemePreferences
import ru.eafedorova.whatifapp.notifications.NotificationScheduler
import ru.eafedorova.whatifapp.presentation.navigation.NavGraph
import ru.eafedorova.whatifapp.presentation.ui.theme.WhatIfAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themePrefs = ThemePreferences(this)

        lifecycleScope.launch {
            val hour = themePrefs.getReminderHour()
            val minute = themePrefs.getReminderMinute()
            val frequency = themePrefs.getReminderFrequency()
            val enabled = themePrefs.getNotificationsEnabled()

            if (enabled) {
                NotificationScheduler.schedule(this@MainActivity, frequency, hour, minute)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        setContent {
            val context = this
            val themePrefs = remember { ThemePreferences(context) }
            val themeMode by themePrefs.themeFlow.collectAsState(initial = "system")

            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            android.util.Log.d("THEME_DEBUG", "themeMode=$themeMode, darkTheme=$darkTheme")

            WhatIfAppTheme(darkTheme = darkTheme) {
                NavGraph()
            }
        }
    }
}