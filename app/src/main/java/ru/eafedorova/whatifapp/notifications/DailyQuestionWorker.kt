package ru.eafedorova.whatifapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.eafedorova.whatifapp.R
import ru.eafedorova.whatifapp.data.AppDatabase
import kotlinx.coroutines.runBlocking

class DailyQuestionWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val context = applicationContext
        val db = AppDatabase.getInstance(context)

        val question = runBlocking {
            db.questionDao().getRandom()
        } ?: return Result.success()

        showNotification(context, question.text)
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

        notificationManager.notify(1, notification)
    }
}