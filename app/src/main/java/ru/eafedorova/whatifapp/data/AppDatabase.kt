package ru.eafedorova.whatifapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Question::class, Answer::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE answers ADD COLUMN voicePath TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            instance?.let { return it }

            synchronized(LOCK) {
                instance?.let { return it }

                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whatifapp.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                instance = newInstance
                return newInstance
            }
        }
    }
}