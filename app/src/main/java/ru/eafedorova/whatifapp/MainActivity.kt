package ru.eafedorova.whatifapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.data.ThemePreferences
import ru.eafedorova.whatifapp.notifications.NotificationScheduler
import ru.eafedorova.whatifapp.presentation.navigation.NavGraph
import ru.eafedorova.whatifapp.presentation.screens.SplashScreen
import ru.eafedorova.whatifapp.presentation.ui.theme.WhatIfAppTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition { false }

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

            WhatIfAppTheme(darkTheme = darkTheme) {
                var showSplash by remember { mutableStateOf(true) }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                    },
                    label = "SplashTransition"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            darkTheme = darkTheme,
                            onAnimationFinished = { showSplash = false }
                        )
                    } else {
                        NavGraph()
                    }
                }
            }
        }
    }
}