package com.example.kursovaya

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [UserProfile::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("ALTER TABLE user_profiles ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN hairColor INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN beardColor INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN hairScale REAL NOT NULL DEFAULT 1.0")
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN beardScale REAL NOT NULL DEFAULT 1.0")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN hairOffsetX REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN hairOffsetY REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN beardOffsetX REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN beardOffsetY REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user_profile_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
