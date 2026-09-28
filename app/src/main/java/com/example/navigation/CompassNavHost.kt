package com.example.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.compass.CompassScreen
import com.example.ui.settings.SettingsScreen

private const val SLIDE_DURATION = 320
private const val FADE_IN_DURATION = 280
private const val FADE_OUT_DURATION = 240

@Composable
fun CompassNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Compass.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { (it * 0.35f).toInt() },
                animationSpec = tween(SLIDE_DURATION, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(FADE_IN_DURATION, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -(it * 0.25f).toInt() },
                animationSpec = tween(SLIDE_DURATION, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(FADE_OUT_DURATION, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -(it * 0.25f).toInt() },
                animationSpec = tween(SLIDE_DURATION, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(FADE_IN_DURATION, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { (it * 0.35f).toInt() },
                animationSpec = tween(SLIDE_DURATION, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(FADE_OUT_DURATION, easing = FastOutSlowInEasing))
        }
    ) {
        composable(route = Screen.Compass.route) {
            CompassScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
