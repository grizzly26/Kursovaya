package com.example.kursovaya

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
//noinspection ExifInterface
import android.media.ExifInterface
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: Executor = ContextCompat.getMainExecutor(context)

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraSelector: CameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Log.e("CameraScreen", "CAMERA permission denied")
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            val capture = ImageCapture.Builder().build()
            imageCapture = capture

            previewView?.let {
                preview.surfaceProvider = it.surfaceProvider
            }

            try {
                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    capture
                )
            } catch (e: Exception) {
                Log.e("CameraScreen", "Camera binding failed", e)
            }
        }, cameraExecutor)

        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { context ->
                PreviewView(context).apply {
                    previewView = this
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = true,
            enter = expandIn(
                initialSize = { IntSize(0, 0) },
                animationSpec = tween(durationMillis = 600)
            ),
            exit = shrinkOut(
                targetSize = { IntSize(0, 0) },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Button(
                onClick = {
                    val file = createImageFile(context.cacheDir)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                    imageCapture?.takePicture(
                        outputOptions,
                        cameraExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            @RequiresApi(Build.VERSION_CODES.Q)
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val uri = file.toURI().toString()
                                val correctedFile = correctImageOrientation(file)
                                Log.d("CameraScreen", "Photo saved: ${correctedFile.toURI()}")
                                navController.navigate("${Screen.StyleMatch.route}?uri=${correctedFile.toURI()}")
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraScreen", "Photo capture failed", exception)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Сделать фото", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(durationMillis = 600)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Button(
                onClick = {

                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    cameraProvider?.let {
                        try {
                            it.unbindAll()
                            val preview = Preview.Builder().build()
                            val capture = ImageCapture.Builder().build()

                            it.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )

                            previewView?.let {
                                preview.surfaceProvider = it.surfaceProvider // Обновляем PreviewView
                            }
                            imageCapture = capture // Обновляем imageCapture
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Error while switching camera", e)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Переключить камеру", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка "Назад" с анимацией
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(durationMillis = 600)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Назад", color = Color.White)
            }
        }
    }
}

fun createImageFile(directory: File): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File(directory, "JPEG_${timestamp}.jpg")
}

@RequiresApi(Build.VERSION_CODES.Q)
fun correctImageOrientation(imageFile: File): File {
    val exif = ExifInterface(imageFile)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

    val rotation = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

    val bitmap = BitmapFactory.decodeStream(FileInputStream(imageFile))
    val rotatedBitmap = rotateBitmap(bitmap, rotation)

    val rotatedFile = File(imageFile.parent, "rotated_${imageFile.name}")
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, rotatedFile.outputStream())

    return rotatedFile
}

fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    val matrix = android.graphics.Matrix()
    matrix.postRotate(degrees.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
