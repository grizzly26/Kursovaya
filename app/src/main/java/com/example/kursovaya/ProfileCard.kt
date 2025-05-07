package com.example.kursovaya

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ProfileCard(profile: UserProfile, onClick: () -> Unit, updateProfile: (UserProfile) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Изображение профиля
            AsyncImage(
                model = profile.imageUri,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Колонка с текстом профиля
            Column(
                modifier = Modifier.weight(1f) // Чтобы колонки не сжимались
            ) {
                Text("Имя: ${profile.name}", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), color = Color.Black) // Черный цвет текста и увеличен шрифт
                Text("Прическа: ${profile.hairStyle}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp), color = Color.Black)
                Text("Борода: ${profile.beardStyle}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp), color = Color.Black)
            }

            // Иконка для добавления/удаления из избранных
            IconButton(onClick = {
                scope.launch {
                    val updatedProfile = profile.copy(isFavorite = !profile.isFavorite)
                    db.userProfileDao().updateProfile(updatedProfile)
                    updateProfile(updatedProfile) // Обновляем только один профиль
                }
            }) {
                Icon(
                    imageVector = if (profile.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Избранное",
                    tint = Color.Red
                )
            }
        }
    }
}