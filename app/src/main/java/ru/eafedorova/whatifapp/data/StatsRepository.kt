package ru.eafedorova.whatifapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class StatsRepository(private val db: AppDatabase) {

    suspend fun totalAnswers(): Int = withContext(Dispatchers.IO) {
        db.answerDao().count()
    }

    suspend fun answersToday(): Int = withContext(Dispatchers.IO) {
        db.answerDao().countSince(todayStart())
    }

    suspend fun byCategory(): List<CategoryCount> = withContext(Dispatchers.IO) {
        db.answerDao().countByCategory()
    }

    suspend fun streak(): Int = withContext(Dispatchers.IO) {
        val answers = db.answerDao().getAllOnce()
        if (answers.isEmpty()) return@withContext 0

        val dayMillis = 24 * 60 * 60 * 1000L

        val days = answers
            .map { startOfDay(it.createdAt) }
            .distinct()
            .sortedDescending()

        var streak = 0
        var expected = todayStart()

        for (day in days) {
            when {
                day == expected -> {
                    streak++
                    expected -= dayMillis
                }
                day < expected -> break
            }
        }
        streak
    }

    private fun startOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun todayStart(): Long = startOfDay(System.currentTimeMillis())

    companion object {
        fun create(context: Context): StatsRepository {
            return StatsRepository(AppDatabase.getInstance(context))
        }
    }
}