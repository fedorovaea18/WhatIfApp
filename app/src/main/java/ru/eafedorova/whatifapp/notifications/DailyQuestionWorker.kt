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
                "Минута для себя",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Тихое напоминание о новом вопросе"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(
                android.graphics.BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_logo
                )
            )
            .setContentTitle("Пять минут с собой")
            .setContentText("Новый вопрос ждёт тебя. Что если бы ты...")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Новый вопрос ждёт тебя.\n Что если бы ты..."))
            .setColor(android.graphics.Color.parseColor("#805A32"))
            .setContentIntent(pendingIntent)
            .addAction(0, "Открыть", pendingIntent)
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