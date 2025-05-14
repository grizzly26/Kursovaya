package com.example.kursovaya

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUri: String,           // Путь к изображению
    val name: String,               // Имя пользователя
    val hairStyle: String,          // Прическа (имя стиля или путь к ресурсу)
    val beardStyle: String,         // Борода (имя стиля или путь к ресурсу)
    val hairColor: Long,            // Цвет волос (в формате Long)
    val beardColor: Long,           // Цвет бороды (в формате Long)
    val isFavorite: Boolean = false, // Пометка как избранное (если нужно)
    val createdAt: Long = System.currentTimeMillis() // Дата создания
)
