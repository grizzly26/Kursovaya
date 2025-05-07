package com.example.kursovaya

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ImageStyleSelector(
    styles: List<Pair<String, Int>>, // Список стилей с их названиями и ресурсами изображений
    selected: Int?, // ID выбранного стиля
    onSelect: (String, Int) -> Unit // Обработчик выбора, который передает имя стиля и его ресурс
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp), // Расстояние между элементами
        contentPadding = PaddingValues(horizontal = 8.dp) // Отступы для контента
    ) {
        itemsIndexed(styles) { _, (name, resId) ->
            val isSelected = selected == resId

            // Используем правильный тип для elevation
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .size(100.dp), // Размер карточки
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color.Gray.copy(alpha = 0.3f) else Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .border(2.dp, if (isSelected) Color.Blue else Color.Gray)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onSelect(name, resId) }
                ) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
