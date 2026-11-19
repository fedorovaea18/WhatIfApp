package ru.eafedorova.whatifapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.eafedorova.whatifapp.R
import ru.eafedorova.whatifapp.data.AppDatabase
import java.util.Calendar

class DailyQuestionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val frequency = inputData.getString("frequency") ?: "daily"

        if (frequency == "weekdays") {
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (today == Calendar.SATURDAY || today == Calendar.SUNDAY) {
                return Result.success()
            }
        }

        val db = AppDatabase.getInstance(applicationContext)
        val question = db.questionDao().getRandom()

        val questionText = question?.text ?: "Что если...?"

        showNotification(applicationContext, questionText)

        return Result.success()
    }

    private fun showNotification(context: Context, questionText: String) {
        val channelId = "daily_question"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Вопрос дня",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ежедневный вопрос для размышления"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Вопрос дня")
            .setContentText(questionText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(questionText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(1, notification)
            }
        } else {
            notificationManager.notify(1, notification)
        }
    }
}