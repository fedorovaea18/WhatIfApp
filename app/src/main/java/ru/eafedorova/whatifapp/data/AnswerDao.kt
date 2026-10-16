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
}