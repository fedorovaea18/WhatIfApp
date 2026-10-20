package ru.eafedorova.whatifapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuestionRepository(private val db: AppDatabase) {

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val total = db.questionDao().count()
        val allGeneral = db.questionDao().countGeneral() == total

        if (total == 0 || allGeneral) {
            db.questionDao().deleteAll()
            db.questionDao().insertAll(
                seedQuestions.map { Question(text = it.text, category = it.category) }
            )
        }
    }

    suspend fun randomQuestion(categories: Set<String>? = null): Question? = withContext(Dispatchers.IO) {
        if (categories.isNullOrEmpty() || categories.contains("all")) {
            db.questionDao().getRandom()
        } else {
            db.questionDao().getRandomByCategories(categories.toList())
        }
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