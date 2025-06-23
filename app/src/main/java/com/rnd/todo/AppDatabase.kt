package com.rnd.todo


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [TodoItem::class], version = 2, exportSchema = true) // Bump version, set exportSchema true for schema files
abstract class AppDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Define the migration
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val currentTime = System.currentTimeMillis()
                // Add new columns with a default value for existing rows
                db.execSQL("ALTER TABLE todo_items ADD COLUMN creation_date INTEGER NOT NULL DEFAULT $currentTime")
                db.execSQL("ALTER TABLE todo_items ADD COLUMN update_date INTEGER NOT NULL DEFAULT $currentTime")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_database"
                )
                    .addMigrations(MIGRATION_1_2) // Add the migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}