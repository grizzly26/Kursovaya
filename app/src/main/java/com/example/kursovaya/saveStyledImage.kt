import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

suspend fun saveStyledImage(
    baseBitmap: ImageBitmap?,
    hairRes: Int?,
    beardRes: Int?,
    hairOffset: Offset,
    beardOffset: Offset,
    hairScale: Float,
    beardScale: Float,
    context: Context
): String? {
    return withContext(Dispatchers.IO) {
        try {
            // Проверяем, что базовое изображение существует
            if (baseBitmap == null) return@withContext null
            val baseAndroidBitmap = baseBitmap.asAndroidBitmap()

            // Создаем новый мутабельный битмап для композитинга
            val resultBitmap = createBitmap(baseAndroidBitmap.width, baseAndroidBitmap.height)
            val canvas = Canvas(resultBitmap)
            val paint = Paint().apply {
                isAntiAlias = true
            }

            // Рисуем базовое изображение
            canvas.drawBitmap(baseAndroidBitmap, 0f, 0f, paint)

            // Рисуем прическу, если выбрана
            hairRes?.let { resId ->
                val hairBitmap = BitmapFactory.decodeResource(context.resources, resId)
                val scaledHairBitmap = hairBitmap.scale(
                    (hairBitmap.width * hairScale).toInt(),
                    (hairBitmap.height * hairScale).toInt(),
                    false
                )
                canvas.drawBitmap(
                    scaledHairBitmap,
                    hairOffset.x,
                    hairOffset.y,
                    paint
                )
                scaledHairBitmap.recycle()  // Освобождаем ресурсы после использования
            }

            // Рисуем бороду, если выбрана
            beardRes?.let { resId ->
                val beardBitmap = BitmapFactory.decodeResource(context.resources, resId)
                val scaledBeardBitmap = beardBitmap.scale(
                    (beardBitmap.width * beardScale).toInt(),
                    (beardBitmap.height * beardScale).toInt(),
                    false
                )
                canvas.drawBitmap(
                    scaledBeardBitmap,
                    beardOffset.x,
                    beardOffset.y,
                    paint
                )
                scaledBeardBitmap.recycle()  // Освобождаем ресурсы после использования
            }

            // Сохраняем итоговое изображение
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(context.filesDir, "profile_$timeStamp.jpg")
            FileOutputStream(file).use { out ->
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            // Освобождаем ресурсы
            resultBitmap.recycle()

            // Возвращаем путь к сохраненному файлу
            file.absolutePath
        } catch (e: Exception) {
            Log.e("SaveStyledImage", "Ошибка сохранения изображения: ${e.message}")
            null
        }
    }
}
