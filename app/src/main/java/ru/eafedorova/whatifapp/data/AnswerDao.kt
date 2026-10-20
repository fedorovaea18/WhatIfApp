package ru.eafedorova.whatifapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnswerDao {
    @Insert
    suspend fun insert(answer: Answer)

    @Query("SELECT * FROM answers ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Answer>>

    @Query("SELECT COUNT(*) FROM answers")
    suspend fun count(): Int

    @Query("DELETE FROM answers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM answers WHERE createdAt >= :since")
    suspend fun countSince(since: Long): Int

    @Query("SELECT * FROM answers ORDER BY createdAt DESC")
    suspend fun getAllOnce(): List<Answer>

    @Query("""
        SELECT q.category, COUNT(*) as count
        FROM answers a
        JOIN questions q ON a.questionId = q.id
        GROUP BY q.category
        ORDER BY count DESC
    """)
    suspend fun countByCategory(): List<CategoryCount>
}