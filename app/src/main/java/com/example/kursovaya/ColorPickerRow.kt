package com.example.kursovaya

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColorPickerRow(
    onHairColorSelected: (Color) -> Unit,
    onBeardColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color.Black, Color.DarkGray, Color.Gray, Color.LightGray,
        Color.Red, Color(0xFFFFA500), Color.Yellow, Color.Green,
        Color.Blue, Color.Cyan, Color.Magenta, Color.White,
        Color(0xFFFFC0CB), Color(0xFF8A2BE2), Color(0xFFFF6347),
        Color(0xFF008080), Color(0xFFFFD700), Color(0xFF8B4513),
        Color(0xFF7FFF00), Color(0xFF00CED1), Color(0xFFADFF2F),
        Color(0xFFFF1493), Color(0xFFDAA520), Color(0xFFFF8C00),
        Color(0xFF9932CC), Color(0xFF3CB371), Color(0xFFFF4500),
        Color(0xFF20B2AA), Color(0xFF4682B4)
    )

    val customTitleStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color.Black
    )

    var showHairDialog by remember { mutableStateOf(false) }
    var showBeardDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {

        Text("Цвет прически", style = customTitleStyle)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Добавляем пункт "естественный"
            ColorCircle(null) {
                showHairDialog = true
                onHairColorSelected(Color.Unspecified)
            }

            colors.forEach { color ->
                ColorCircle(color) {
                    onHairColorSelected(color)
                }
            }
        }

        Text("Цвет бороды", style = customTitleStyle, modifier = Modifier.padding(top = 8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorCircle(null) {
                showBeardDialog = true
                onBeardColorSelected(Color.Unspecified)
            }

            colors.forEach { color ->
                ColorCircle(color) {
                    onBeardColorSelected(color)
                }
            }
        }

        if (showHairDialog) {
            AlertDialog(
                onDismissRequest = { showHairDialog = false },
                title = { Text("Естественный цвет") },
                text = { Text("Вы выбрали естественный цвет причёски. Цвет будет по умолчанию.") },
                confirmButton = {
                    TextButton(onClick = { showHairDialog = false }) {
                        Text("ОК")
                    }
                }
            )
        }

        if (showBeardDialog) {
            AlertDialog(
                onDismissRequest = { showBeardDialog = false },
                title = { Text("Естественный цвет") },
                text = { Text("Вы выбрали естественный цвет бороды. Цвет будет по умолчанию.") },
                confirmButton = {
                    TextButton(onClick = { showBeardDialog = false }) {
                        Text("ОК")
                    }
                }
            )
        }
    }
}

@Composable
fun ColorCircle(color: Color?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color ?: Color.LightGray)
            .border(2.dp, Color.Black, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (color == null) {
            Text("E", fontSize = 14.sp, color = Color.Black)
        }
    }
}
