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

private const val TRANSITION_DURATION = 300
private const val FADE_DURATION = 250

@Composable
fun CompassNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Compass.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = Screen.Compass.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -(it * 0.12f).toInt() },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -(it * 0.12f).toInt() },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -(it * 0.12f).toInt() },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -(it * 0.12f).toInt() },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            }
        ) {
            CompassScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.15f).toInt() },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.15f).toInt() },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.15f).toInt() },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.15f).toInt() },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            }
        ) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
