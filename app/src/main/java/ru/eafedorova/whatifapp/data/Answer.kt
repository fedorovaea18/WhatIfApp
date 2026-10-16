package ru.eafedorova.whatifapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "answers")
data class Answer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Long,
    val questionText: String,
    val answerText: String,
    val createdAt: Long = System.currentTimeMillis()
)