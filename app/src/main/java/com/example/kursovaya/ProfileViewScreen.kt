package com.example.kursovaya

import android.content.Intent
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import java.io.File
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import java.lang.ProcessBuilder.Redirect.to


@Composable
fun ProfileViewScreen(navController: NavController, profileId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val textVisibilityState = remember { mutableStateOf(false) }

    // Загрузка профиля
    LaunchedEffect(profileId) {
        scope.launch {
            profile = db.userProfileDao().getProfileById(profileId)
            if (profile != null) {
                Log.d("ProfileViewScreen", "Загружен профиль: ${profile?.name}, hair: ${profile?.hairStyle}, beard: ${profile?.beardStyle}")
            } else {
                Log.d("ProfileViewScreen", "Профиль не найден!")
            }
        }
    }

    profile?.let { profile ->
        LaunchedEffect(profile) {
            delay(300) // Задержка для красивой анимации
            textVisibilityState.value = true
        }

        // Фон картинки
        Image(
            painter = rememberAsyncImagePainter(R.drawable.uri), // Ваш ресурс для фона
            contentDescription = "Фон",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Заголовок страницы с анимацией
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Основное изображение с наложением прически и бороды
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Увеличим высоту для более четкого отображения
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showFullImage = true }
                    .padding(8.dp)
            ) {
                // Основное изображение профиля
                AsyncImage(
                    model = profile.imageUri, // используем сохраненный URI
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),  // Обеспечивает сохранение округленных углов
                    contentScale = ContentScale.Crop
                )


            }

            Spacer(modifier = Modifier.height(16.dp))

            // Имя с анимацией и задержкой
            AnimatedVisibility(
                visible = textVisibilityState.value,
                enter = fadeIn(
                    initialAlpha = 0f,
                    animationSpec = tween(durationMillis = 800, delayMillis = 500)
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                Text(
                    "Имя: ${profile.name}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.1f
                    )
                )
            }

            // Прическа с анимацией и задержкой
            AnimatedVisibility(
                visible = textVisibilityState.value,
                enter = fadeIn(
                    initialAlpha = 0f,
                    animationSpec = tween(durationMillis = 800, delayMillis = 800)
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                Text(
                    "Прическа: ${profile.hairStyle}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.1f
                    )
                )
            }

            // Борода с анимацией и задержкой
            AnimatedVisibility(
                visible = textVisibilityState.value,
                enter = fadeIn(
                    initialAlpha = 0f,
                    animationSpec = tween(durationMillis = 800, delayMillis = 1200)
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                Text(
                    "Борода: ${profile.beardStyle}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.1f
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопки с анимацией
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Назад", style = MaterialTheme.typography.bodyMedium)
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        onClick = {
                            navController.navigate("edit_profile/${profileId}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Редактировать", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        val file = File(profile.imageUri.toUri().path ?: "")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Имя: ${profile.name}\nПрическа: ${profile.hairStyle}\nБорода: ${profile.beardStyle}"
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        context.startActivity(
                            Intent.createChooser(shareIntent, "Поделиться через")
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Поделиться", style = MaterialTheme.typography.bodyMedium)
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = { showDialog = true }
                ) {
                    Text("Удалить", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Диалог подтверждения удаления
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Удаление профиля") },
                text = { Text("Вы уверены, что хотите удалить этот профиль?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            db.userProfileDao().deleteProfile(profile)
                            navController.popBackStack()
                        }
                    }) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        // Полноэкранное изображение
        if (showFullImage) {
            Dialog(onDismissRequest = { showFullImage = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showFullImage = false },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(profile.imageUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


