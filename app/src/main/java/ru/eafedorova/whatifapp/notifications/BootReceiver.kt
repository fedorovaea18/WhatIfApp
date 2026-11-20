package ru.eafedorova.whatifapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.data.ThemePreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                try {
                    val prefs = ThemePreferences(context)
                    val notificationsEnabled = prefs.getNotificationsEnabled()

                    if (notificationsEnabled) {

                        val frequency = prefs.getReminderFrequency()
                        val hour = prefs.getReminderHour()
                        val minute = prefs.getReminderMinute()

                        NotificationScheduler.schedule(
                            context,
                            frequency,
                            hour,
                            minute
                        )
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}