package com.example.navigation

sealed class Screen(val route: String) {
    data object Compass : Screen("compass")
    data object Settings : Screen("settings")
}
