package com.example.kursovaya

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import saveStyledImage
import java.io.File
import java.net.URI
import kotlin.math.roundToInt





@Composable
fun StyleMatchScreen(
    navController: NavController,
    uri: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)
    val userProfileDao = db.userProfileDao()

    val OffsetSaver = Saver<Offset, List<Float>>(
        save = { listOf(it.x, it.y) },
        restore = { Offset(it[0], it[1]) }
    )

    val ColorSaver = Saver<Color, Long>(
        save = { it.value.toLong() },
        restore = { Color(it) }
    )

    // Состояние для других компонентов
    var hairstyle by rememberSaveable { mutableStateOf("") }
    var beardStyle by rememberSaveable { mutableStateOf("") }
    var selectedHairRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedBeardRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var profileName by rememberSaveable { mutableStateOf("") }

    // Спасаем URI при изменении конфигурации
    var savedUri by remember { mutableStateOf(uri) }

    val defaultColor = Color.Unspecified
    var selectedHairColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(defaultColor) }
    var selectedBeardColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(defaultColor) }

    var hairOffset by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }
    var beardOffset by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }

    var hairScale by rememberSaveable { mutableStateOf(1f) }
    var beardScale by rememberSaveable { mutableStateOf(1f) }

    // Состояние для изображения
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Загружаем изображение только один раз, но без сброса при повороте экрана
    LaunchedEffect(savedUri) {
        try {
            val file = File(URI(savedUri))
            val bmp = BitmapFactory.decodeFile(file.path)
            bitmap = bmp?.asImageBitmap()
        } catch (e: Exception) {
            Log.e("StyleMatchScreen", "Ошибка загрузки изображения", e)
            Toast.makeText(context, "Ошибка анализа фото", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Подбор стиля", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        bitmap?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                Image(
                    painter = BitmapPainter(it),
                    contentDescription = "Фото",
                    modifier = Modifier.matchParentSize()
                )

                selectedHairRes?.let { res ->
                    Image(
                        painter = painterResource(id = res),
                        contentDescription = "Hair",
                        colorFilter = if (selectedHairColor != defaultColor) {
                            ColorFilter.tint(selectedHairColor, BlendMode.SrcIn)
                        } else null,
                        modifier = Modifier
                            .offset { IntOffset(hairOffset.x.roundToInt(), hairOffset.y.roundToInt()) }
                            .graphicsLayer(scaleX = hairScale, scaleY = hairScale)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    hairOffset += pan
                                    hairScale *= zoom
                                }
                            }
                            .size(100.dp)
                    )
                }

                selectedBeardRes?.let { res ->
                    Image(
                        painter = painterResource(id = res),
                        contentDescription = "Beard",
                        colorFilter = if (selectedBeardColor != defaultColor) {
                            ColorFilter.tint(selectedBeardColor, BlendMode.SrcIn)
                        } else null,
                        modifier = Modifier
                            .offset { IntOffset(beardOffset.x.roundToInt(), beardOffset.y.roundToInt()) }
                            .graphicsLayer(scaleX = beardScale, scaleY = beardScale)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    beardOffset += pan
                                    beardScale *= zoom
                                }
                            }
                            .size(100.dp)
                    )
                }
            }
        } ?: Text("Фото не загружено")

        Spacer(modifier = Modifier.height(16.dp))

        val hairstyles = listOf(
            "Андеркат" to R.drawable.hair1,
            "Классика" to R.drawable.hair2,
            "Кудри" to R.drawable.hair3,
            "Короткая стрижка" to R.drawable.hair4,
            "Бокс" to R.drawable.hair5,
            "Полубокс" to R.drawable.hair6,
            "Ирокез" to R.drawable.hair7,
            "Гаврош" to R.drawable.hair8,
            "Модерн" to R.drawable.hair9,
            "Ретро" to R.drawable.hair10
        )

        val beards = listOf(
            "Короткая борода" to R.drawable.beard1,
            "Щетина" to R.drawable.beard2,
            "Полная борода" to R.drawable.beard3,
            "Без бороды" to R.drawable.beard4,
            "Козлиная бородка" to R.drawable.beard5,
            "Усы и борода" to R.drawable.beard6,
            "Бальбо" to R.drawable.beard7,
            "Якорь" to R.drawable.beard8,
            "Шеврон" to R.drawable.beard9,
            "Гарибальди" to R.drawable.beard10
        )

        ImageStyleSelector(styles = hairstyles, selected = selectedHairRes) { name, res ->
            hairstyle = name
            selectedHairRes = res
        }

        ImageStyleSelector(styles = beards, selected = selectedBeardRes) { name, res ->
            beardStyle = name
            selectedBeardRes = res
        }

        ColorPickerRow(
            onHairColorSelected = { selectedHairColor = it },
            onBeardColorSelected = { selectedBeardColor = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = profileName,
            onValueChange = { profileName = it },
            label = { Text("Введите имя профиля") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (profileName.isEmpty() || hairstyle.isEmpty() || beardStyle.isEmpty()) {
                    Toast.makeText(context, "Пожалуйста, введите имя профиля и выберите стиль", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (bitmap == null || selectedHairRes == null || selectedBeardRes == null) {
                    Toast.makeText(context, "Ошибка: изображение или ресурсы не выбраны", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch {
                    try {
                        val savedImagePath = withContext(Dispatchers.IO) {
                            saveStyledImage(
                                bitmap,
                                selectedHairRes,
                                selectedBeardRes,
                                hairOffset,
                                beardOffset,
                                hairScale,
                                beardScale,
                                context
                            )
                        }

                        if (savedImagePath != null) {
                            val profile = UserProfile(
                                name = profileName,
                                imageUri = savedImagePath,
                                hairStyle = hairstyle,
                                beardStyle = beardStyle,
                                hairColor = selectedHairColor.value.toLong(),
                                beardColor = selectedBeardColor.value.toLong(),
                                hairScale = hairScale,
                                beardScale = beardScale,
                                hairOffsetX = hairOffset.x,
                                hairOffsetY = hairOffset.y,
                                beardOffsetX = beardOffset.x,
                                beardOffsetY = beardOffset.y
                            )
                            withContext(Dispatchers.IO) {
                                userProfileDao.insertProfile(profile)
                            }
                            Toast.makeText(context, "Профиль сохранён", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ошибка сохранения изображения", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Произошла ошибка при сохранении", Toast.LENGTH_SHORT).show()
                        Log.e("SaveProfile", "Ошибка: ${e.message}, profileName: $profileName")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(
                "Сохранить профиль",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }



        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            )
        ) {
            Text("Назад", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
        }
    }
}



