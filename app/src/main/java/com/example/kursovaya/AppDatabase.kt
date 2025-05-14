package com.example.kursovaya

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [UserProfile::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавление новых столбцов
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN hairColor INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN beardColor INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Создание новой таблицы
                database.execSQL("""
                    CREATE TABLE user_profiles_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        imageUri TEXT NOT NULL,
                        name TEXT NOT NULL,
                        hairStyle TEXT NOT NULL,
                        beardStyle TEXT NOT NULL,
                        hairColor INTEGER NOT NULL,
                        beardColor INTEGER NOT NULL,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """)

                database.execSQL("""
                    INSERT INTO user_profiles_new (id, imageUri, name, hairStyle, beardStyle, hairColor, beardColor, isFavorite, createdAt)
                    SELECT id, imageUri, name, hairStyle, beardStyle, hairColor, beardColor, isFavorite, createdAt
                    FROM user_profiles
                """)

                database.execSQL("DROP TABLE user_profiles")

                database.execSQL("ALTER TABLE user_profiles_new RENAME TO user_profiles")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE user_profiles_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        imageUri TEXT NOT NULL,
                        name TEXT NOT NULL,
                        hairStyle TEXT NOT NULL,
                        beardStyle TEXT NOT NULL,
                        hairColor INTEGER NOT NULL,
                        beardColor INTEGER NOT NULL,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """)

                // Переносим данные из старой таблицы в новую
                database.execSQL("""
                    INSERT INTO user_profiles_new (id, imageUri, name, hairStyle, beardStyle, hairColor, beardColor, isFavorite, createdAt)
                    SELECT id, imageUri, name, hairStyle, beardStyle, hairColor, beardColor, isFavorite, createdAt
                    FROM user_profiles
                """)

                // Удаляем старую таблицу
                database.execSQL("DROP TABLE user_profiles")

                // Переименовываем новую таблицу
                database.execSQL("ALTER TABLE user_profiles_new RENAME TO user_profiles")
            }
        }

        // Получение экземпляра базы данных
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user_profile_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // Добавляем миграции
                    .fallbackToDestructiveMigration() // Если миграции не работают, можно очистить базу
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
