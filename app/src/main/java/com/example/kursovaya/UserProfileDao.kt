package com.example.kursovaya

import androidx.room.*

@Dao
interface UserProfileDao {

    @Insert
    suspend fun insertProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profiles ORDER BY id DESC")
    suspend fun getAllProfiles(): List<UserProfile>

    @Query("SELECT * FROM user_profiles WHERE id = :profileID")
    suspend fun getProfileById(profileID: Int): UserProfile

    @Query("SELECT * FROM user_profiles WHERE imageUri = :uri LIMIT 1")
    suspend fun getByImageUri(uri: String): UserProfile?

    @Delete
    suspend fun deleteProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE isFavorite = 1")
    suspend fun getFavoriteProfiles(): List<UserProfile>

}
