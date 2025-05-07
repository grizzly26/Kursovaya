package com.example.kursovaya

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(navController: NavController, profileId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }

    var name by remember { mutableStateOf("") }
    var hairStyle by remember { mutableStateOf("") }
    var beardStyle by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(profileId) {
        profile = db.userProfileDao().getProfileById(profileId)
        profile?.let {
            name = it.name
            hairStyle = it.hairStyle
            beardStyle = it.beardStyle
        }
    }

    profile?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage.isNotEmpty()
            )
            OutlinedTextField(
                value = hairStyle,
                onValueChange = { hairStyle = it },
                label = { Text("Прическа") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage.isNotEmpty()
            )
            OutlinedTextField(
                value = beardStyle,
                onValueChange = { beardStyle = it },
                label = { Text("Борода") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage.isNotEmpty()
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        if (name.isBlank() || hairStyle.isBlank() || beardStyle.isBlank()) {
                            errorMessage = "Все поля должны быть заполнены"
                        } else {
                            scope.launch {
                                val updatedProfile = it.copy(
                                    name = name,
                                    hairStyle = hairStyle,
                                    beardStyle = beardStyle
                                )
                                db.userProfileDao().updateProfile(updatedProfile)
                                navController.popBackStack()
                            }
                        }
                    }
                ) {
                    Text("Сохранить")
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
