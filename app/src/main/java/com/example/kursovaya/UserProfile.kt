package com.example.kursovaya

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUri: String,
    val name: String,
    val hairStyle: String,          // Прическа (имя стиля или путь к ресурсу)
    val beardStyle: String,         // Борода (имя стиля или путь к ресурсу)
    val hairColor: Long,            // Цвет волос (цвет в long формате)
    val beardColor: Long,           // Цвет бороды (цвет в long формате)
    val hairScale: Float = 1f,      // Масштаб для прически
    val beardScale: Float = 1f,     // Масштаб для бороды
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(), // Дата создания
    val hairOffsetX: Float = 0f,      // Смещение прически по X
    val hairOffsetY: Float = 0f,      // Смещение прически по Y
    val beardOffsetX: Float = 0f,     // Смещение бороды по X
    val beardOffsetY: Float = 0f      // Смещение бороды по Y

)

