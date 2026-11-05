package ru.eafedorova.whatifapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandom(): Question?

    @Query("SELECT * FROM questions WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomByCategory(category: String): Question?

    @Query("SELECT * FROM questions WHERE category IN (:categories) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomByCategories(categories: List<String>): Question?

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Question?

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE category = 'general'")
    suspend fun countGeneral(): Int

    @Query("SELECT category, COUNT(*) as count FROM questions GROUP BY category")
    suspend fun countQuestionsByCategory(): List<CategoryCount>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(questions: List<Question>)

    @Query("DELETE FROM questions")
    suspend fun deleteAll()
}