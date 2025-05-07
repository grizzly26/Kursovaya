package com.example.kursovaya

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavController) {
    val backgroundImage: Painter = painterResource(id = R.drawable.your_background)
    var visible by remember { mutableStateOf(false) }

    // Плавная анимация появления после задержки
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = backgroundImage,
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xAA000000), Color.Transparent, Color(0xAA000000))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(600))
            ) {
                Text(
                    "Главная",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) +
                        slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 100))
            ) {
                AnimatedStyledButton(
                    text = "Открыть камеру",
                    onClick = { navController.navigate(Screen.Camera.route) }
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) +
                        slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 250))
            ) {
                AnimatedStyledButton(
                    text = "Готовые профили",
                    onClick = { navController.navigate(Screen.Profiles.route) }
                )
            }
        }
    }
}

@Composable
fun AnimatedStyledButton(text: String, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val buttonColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF03DAC5) else Color.White,
        label = "buttonColorAnimation"
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = if (isPressed) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}
