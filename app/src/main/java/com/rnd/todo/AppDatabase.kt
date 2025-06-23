package com.rnd.todo


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [TodoItem::class], version = 3, exportSchema = true) // Version incremented
abstract class AppDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) { /* ... your existing migration ... */
            override fun migrate(db: SupportSQLiteDatabase) {
                val currentTime = System.currentTimeMillis()
                db.execSQL("ALTER TABLE todo_items ADD COLUMN creation_date INTEGER NOT NULL DEFAULT $currentTime")
                db.execSQL("ALTER TABLE todo_items ADD COLUMN update_date INTEGER NOT NULL DEFAULT $currentTime")
            }
        }

        // New Migration for version 2 to 3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the new dueDate column, allowing NULL values
                // Defaulting to NULL as existing tasks won't have a due date
                db.execSQL("ALTER TABLE todo_items ADD COLUMN due_date INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Add new migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}