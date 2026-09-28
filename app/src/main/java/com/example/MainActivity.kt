package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.CompassPreferences
import com.example.navigation.CompassNavHost
import com.example.ui.theme.AppThemeState
import com.example.ui.theme.CompassTheme
import com.example.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CompassPreferences.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val isDark = when (AppThemeState.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CompassTheme(
                darkTheme = isDark,
                dynamicColor = AppThemeState.dynamicColorEnabled
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    CompassNavHost()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CompassPreferences.checkAndEnforcePermissionState(applicationContext)
    }
}
