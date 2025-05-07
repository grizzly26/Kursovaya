package com.example.kursovaya

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var profiles by remember { mutableStateOf(listOf<UserProfile>()) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) } // Для анимированной загрузки

    val scope = rememberCoroutineScope()

    // Функция загрузки данных
    fun loadProfiles() {
        scope.launch {
            val allProfiles = if (showOnlyFavorites) {
                db.userProfileDao().getFavoriteProfiles()
            } else {
                db.userProfileDao().getAllProfiles()
            }
            profiles = allProfiles.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
            isLoading = false // Загрузка завершена
        }
    }

    LaunchedEffect(showOnlyFavorites, searchQuery) {
        isLoading = true // Когда запрос на загрузку начинается
        loadProfiles()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Фон с градиентом
        Image(
            painter = painterResource(id = R.drawable.bg_profile), // Замените на ваш фоновый рисунок
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
        )

        // Контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Поиск с иконкой очистки
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск по имени", color = Color.White, fontSize = 18.sp) }, // Увеличен шрифт
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear search",
                                modifier = Modifier.clickable { searchQuery = "" },
                                tint = Color.White // БЕЛАЯ ИКОНКА
                            )
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        cursorColor = Color.Cyan,
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }

            // Переключатель "Показать только избранные"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Показать только избранные", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) // Увеличен шрифт
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = showOnlyFavorites,
                    onCheckedChange = { showOnlyFavorites = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Red, uncheckedThumbColor = Color.Gray),
                    modifier = Modifier.size(50.dp) // Увеличен размер переключателя
                )
            }

            // Анимированная загрузка
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            // Список профилей с анимацией
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfileCard(
                        profile = profile,
                        onClick = { navController.navigate("profile_view/${profile.id}") },
                        updateProfile = { updatedProfile ->
                            profiles = profiles.map {
                                if (it.id == updatedProfile.id) updatedProfile else it
                            }
                        }
                    )
                }
            }
        }
    }
}

