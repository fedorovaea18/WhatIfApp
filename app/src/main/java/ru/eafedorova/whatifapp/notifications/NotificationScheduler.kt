package ru.eafedorova.whatifapp.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WORK_NAME = "daily_question"

    fun schedule(context: Context, frequency: String, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }

            when (frequency) {
                "weekdays" -> {
                    while (get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
                "weekly" -> {

                    while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            }
        }

        val delay = target.timeInMillis - now.timeInMillis

        val repeatInterval = when (frequency) {
            "weekdays" -> 1L
            "weekly" -> 7L
            else -> 1L
        }


        val requestBuilder = PeriodicWorkRequestBuilder<DailyQuestionWorker>(
            repeatInterval, TimeUnit.DAYS
        ).setInitialDelay(delay, TimeUnit.MILLISECONDS)

        val data = androidx.work.Data.Builder()
            .putString("frequency", frequency)
            .build()
        requestBuilder.setInputData(data)

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            requestBuilder.build()
        )
    }

    fun cancelDailyQuestion(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}