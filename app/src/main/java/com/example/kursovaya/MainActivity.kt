package com.example.kursovaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.kursovaya.ui.theme.KursovayaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KursovayaTheme {
                    val navController = rememberNavController()
                AppNavGraph(navController = navController)
                }
            }
        }
    }


