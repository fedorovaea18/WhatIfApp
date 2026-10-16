package ru.eafedorova.whatifapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuestionRepository(private val db: AppDatabase) {

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (db.questionDao().count() == 0) {
            db.questionDao().insertAll(seedQuestions.map { Question(text = it) })
        }
    }

    suspend fun randomQuestion(): Question? = withContext(Dispatchers.IO) {
        db.questionDao().getRandom()
    }

    suspend fun saveAnswer(answer: Answer) = withContext(Dispatchers.IO) {
        db.answerDao().insert(answer)
    }

    companion object {
        fun create(context: Context): QuestionRepository {
            return QuestionRepository(AppDatabase.getInstance(context))
        }
    }
}