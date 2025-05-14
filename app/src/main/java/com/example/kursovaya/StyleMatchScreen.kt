package com.example.kursovaya

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.scale
import java.io.FileOutputStream
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter


data class Bounds(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float)

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

    // Переменные для отслеживания размеров изображения
    var imageBounds by remember { mutableStateOf<Rect?>(null) }

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
                    contentScale = ContentScale.Crop, // Это важно!
                    modifier = Modifier
                        .matchParentSize()
                        .onGloballyPositioned { layoutCoordinates ->
                            imageBounds = layoutCoordinates.boundsInParent()
                        }
                )


                selectedHairRes?.let { res ->
                    DraggableImage(
                        resId = res,
                        contentDescription = "Hair",
                        initialOffset = hairOffset,
                        initialScale = hairScale,
                        color = selectedHairColor,
                        defaultColor = defaultColor,
                        imageBounds = imageBounds,
                        onOffsetChange = { newOffset -> hairOffset = newOffset },
                        onScaleChange = { newScale -> hairScale = newScale }
                    )
                } ?: run {
                    Log.e("StyleMatchScreen", "Прическа не выбрана!")
                }

                selectedBeardRes?.let { res ->
                    DraggableImage(
                        resId = res,
                        contentDescription = "Beard",
                        initialOffset = beardOffset,
                        initialScale = beardScale,
                        color = selectedBeardColor,
                        defaultColor = defaultColor,
                        imageBounds = imageBounds,
                        onOffsetChange = { newOffset -> beardOffset = newOffset },
                        onScaleChange = { newScale -> beardScale = newScale }
                    )
                } ?: run {
                    Log.e("StyleMatchScreen", "Борода не выбрана!")
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
                if (profileName.isNullOrEmpty() || hairstyle.isEmpty() || beardStyle.isEmpty()) {
                    Toast.makeText(context, "Пожалуйста, введите имя профиля и выберите стиль", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (bitmap == null || selectedHairRes == null || selectedBeardRes == null) {
                    Toast.makeText(context, "Ошибка: изображение или ресурсы не выбраны", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch {
                    try {
                        val savedImageData = withContext(Dispatchers.IO) {
                            mergeImages(
                                baseImage = bitmap!!,
                                hairResId = selectedHairRes!!,
                                beardResId = selectedBeardRes!!,
                                hairScale = hairScale,
                                beardScale = beardScale,
                                hairColor = selectedHairColor,
                                beardColor = selectedBeardColor,
                                context = context
                            )
                        }

                        savedImageData?.let { (savedImagePath, colors) ->
                            val (savedHairColor, savedBeardColor) = colors

                            // Создаем профиль
                            val profile = UserProfile(
                                name = profileName, // Переход к пустой строке, если profileName равно null
                                imageUri = savedImagePath.toString(),
                                hairStyle = hairstyle,
                                beardStyle = beardStyle,
                                hairColor = selectedHairColor.value.toLong(),
                                beardColor = selectedBeardColor.value.toLong()
                            )

                            // Сохраняем профиль в базе данных
                            withContext(Dispatchers.IO) {
                                userProfileDao.insertProfile(profile)
                            }
                            Toast.makeText(context, "Профиль сохранён", Toast.LENGTH_SHORT).show()
                        } ?: run {
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

@Composable
fun DraggableImage(
    resId: Int,
    contentDescription: String,
    initialOffset: Offset,
    initialScale: Float,
    color: Color,
    defaultColor: Color,
    imageBounds: Rect?,
    onOffsetChange: (Offset) -> Unit,
    onScaleChange: (Float) -> Unit
) {
    var offset by remember { mutableStateOf(initialOffset) }
    var scale by remember { mutableStateOf(initialScale) }
    var imageSize by remember { mutableStateOf<androidx.compose.ui.geometry.Size?>(null) }

    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        colorFilter = if (color != defaultColor) {
            ColorFilter.tint(color, BlendMode.SrcIn)
        } else null,
        modifier = Modifier
            .offset { offset.round() }
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .onGloballyPositioned { layoutCoordinates ->
                imageSize = layoutCoordinates.size.toSize()
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = scale * zoom
                    val newOffset = offset + pan

                    val bounds = calculateBounds(imageBounds?.size ?: androidx.compose.ui.geometry.Size(0f, 0f), imageSize, newScale)
                    val boundedOffset = Offset(
                        newOffset.x.coerceIn(bounds.minX, bounds.maxX),
                        newOffset.y.coerceIn(bounds.minY, bounds.maxY)
                    )

                    offset = boundedOffset
                    scale = newScale
                    onOffsetChange(boundedOffset)
                    onScaleChange(newScale)
                }
            }
            .size(100.dp)
    )
}

private fun calculateBounds(
    containerSize: androidx.compose.ui.geometry.Size,
    imageSize: androidx.compose.ui.geometry.Size?,
    scale: Float
): Bounds {
    val imageWidth = imageSize?.width ?: 0f
    val imageHeight = imageSize?.height ?: 0f

    val scaledWidth = imageWidth * scale
    val scaledHeight = imageHeight * scale

    return Bounds(
        minX = 0f - scaledWidth / 2f,
        maxX = containerSize.width - scaledWidth / 2f,
        minY = 0f - scaledHeight / 2f,
        maxY = containerSize.height - scaledHeight / 2f
    )
}



fun mergeImages(
    baseImage: ImageBitmap,
    hairResId: Int,
    beardResId: Int,
    hairScale: Float,
    beardScale: Float,
    hairColor: Color,
    beardColor: Color,
    context: Context
): Pair<String?, Pair<Color, Color>>? {

    // Преобразуем ImageBitmap в Bitmap
    val baseBitmap = baseImage.asAndroidBitmap()

    // Получаем Bitmap для прически и бороды
    val hairBitmap = BitmapFactory.decodeResource(context.resources, hairResId)
    val beardBitmap = BitmapFactory.decodeResource(context.resources, beardResId)

    // Проверяем, что изображения загружены
    if (hairBitmap == null || beardBitmap == null) {
        Log.e("mergeImages", "Ошибка загрузки изображений для прически или бороды")
        return null
    }

    // Масштабируем прическу с уменьшением масштаба (умножаем на 0.9f)
    val adjustedHairScale = hairScale * 0.9f
    val scaledHairBitmap = hairBitmap.scale(
        (hairBitmap.width * adjustedHairScale).toInt(),
        (hairBitmap.height * adjustedHairScale).toInt(),
        false
    )

    // Масштабируем бороду с учетом предоставленного масштаба
    val scaledBeardBitmap = beardBitmap.scale(
        (beardBitmap.width * beardScale).toInt(),
        (beardBitmap.height * beardScale).toInt(),
        false
    )

    // Создаем новый ImageBitmap для итогового изображения
    val resultBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)

    // Рисуем на Canvas
    val canvas = Canvas(resultBitmap)

    // Рисуем исходное изображение на канвасе
    canvas.drawBitmap(baseBitmap, 0f, 0f, null)

    // Если цвет не выбран для прически, рисуем её как есть
    if (hairColor == Color.Unspecified) {
        val hairX = (baseBitmap.width - scaledHairBitmap.width) / 2f
        val hairY = (baseBitmap.height - scaledHairBitmap.height) / 4f // Верхняя часть изображения
        canvas.drawBitmap(scaledHairBitmap, hairX, hairY, null)
    } else {
        // Если цвет выбран, применяем фильтр
        val hairX = (baseBitmap.width - scaledHairBitmap.width) / 2f
        val hairY = (baseBitmap.height - scaledHairBitmap.height) / 4f // Верхняя часть изображения
        val paintHair = Paint().apply {
            colorFilter = PorterDuffColorFilter(hairColor.toArgb(), PorterDuff.Mode.SRC_IN)
        }
        canvas.drawBitmap(scaledHairBitmap, hairX, hairY, paintHair)
    }

    // Если цвет не выбран для бороды, рисуем её как есть
    if (beardColor == Color.Unspecified) {
        val beardX = (baseBitmap.width - scaledBeardBitmap.width) / 2f
        val beardY = (baseBitmap.height - scaledBeardBitmap.height) / 1.5f // Нижняя часть изображения
        canvas.drawBitmap(scaledBeardBitmap, beardX, beardY, null)
    } else {
        // Если цвет выбран, применяем фильтр
        val beardX = (baseBitmap.width - scaledBeardBitmap.width) / 2f
        val beardY = (baseBitmap.height - scaledBeardBitmap.height) / 1.5f // Нижняя часть изображения
        val paintBeard = Paint().apply {
            colorFilter = PorterDuffColorFilter(beardColor.toArgb(), PorterDuff.Mode.SRC_IN)
        }
        canvas.drawBitmap(scaledBeardBitmap, beardX, beardY, paintBeard)
    }

    // Сохраняем итоговое изображение на диск
    val savedFile = saveBitmapToFile(resultBitmap, context)

    // Возвращаем путь к файлу и цвета
    return if (savedFile != null) {
        Pair(savedFile.absolutePath, Pair(hairColor, beardColor))
    } else {
        Log.e("mergeImages", "Ошибка сохранения изображения")
        null
    }
}


// Метод для сохранения Bitmap в файл
fun saveBitmapToFile(bitmap: Bitmap, context: Context): File? {
    // Создаем временный файл
    val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File(directory, "merged_image_${System.currentTimeMillis()}.png")

    try {
        // Сохраняем изображение в файл
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    return file
}
