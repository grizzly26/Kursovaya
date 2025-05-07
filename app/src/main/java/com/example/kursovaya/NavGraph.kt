package com.example.kursovaya

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Home : Screen("home")
    object Camera : Screen("camera")
    object StyleMatch : Screen("style_match")
    object Profiles : Screen("profiles")

    object ProfileView : Screen("profile_view/{profileId}") {
        fun createRoute(profileId: Int) = "profile_view/$profileId"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Welcome.route) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Camera.route) {
            CameraScreen(navController)
        }
        composable(
            "${Screen.StyleMatch.route}?uri={uri}",
            arguments = listOf(navArgument("uri") { defaultValue = "" })
        ) {
            val uri = it.arguments?.getString("uri") ?: ""
            StyleMatchScreen(navController, uri)
        }
        composable(Screen.Profiles.route) {
            ProfileScreen(navController)
        }
        composable(
            route = Screen.ProfileView.route,
            arguments = listOf(navArgument("profileId") { type = NavType.IntType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getInt("profileId") ?: return@composable
            ProfileViewScreen(navController, profileId)
        }

        composable("edit_profile/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            id?.let {
                EditProfileScreen(navController, it)
            }
        }
    }
}
