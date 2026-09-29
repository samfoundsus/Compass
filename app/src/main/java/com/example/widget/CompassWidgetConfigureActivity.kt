package com.example.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CompassPreferences
import com.example.ui.compass.CompassDial
import com.example.ui.compass.CompassDialShape
import com.example.ui.theme.AppThemeState
import com.example.ui.theme.CompassTheme
import com.example.ui.theme.ThemeMode

/**
 * Native configuration activity shown when adding a Compass home-screen widget.
 * Allows the user to select one of the four dial shapes specifically for this widget instance.
 */
class CompassWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CompassPreferences.init(applicationContext)

        // Set the result to CANCELED. If the user backs out before confirming, the widget is not added.
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

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
                var selectedShape by remember {
                    mutableStateOf<CompassDialShape>(CompassDialShape.Sunny)
                }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = stringResource(id = R.string.widget_configure_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 20.dp)
                            ) {
                                Button(
                                    onClick = {
                                        confirmWidgetSelection(selectedShape)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("add_widget_button"),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.widget_configure_add_button),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.widget_configure_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val shapes = listOf(
                            CompassDialShape.Sunny,
                            CompassDialShape.Circle,
                            CompassDialShape.Diamond,
                            CompassDialShape.Octagon
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(shapes) { shape ->
                                val isSelected = shape == selectedShape
                                val shapeName = stringResource(id = shape.nameRes)

                                ShapeSelectionCard(
                                    shape = shape,
                                    shapeName = shapeName,
                                    isSelected = isSelected,
                                    onSelect = { selectedShape = shape }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun confirmWidgetSelection(selectedShape: CompassDialShape) {
        val context = applicationContext
        CompassWidgetPreferences.setWidgetShape(context, appWidgetId, selectedShape)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        CompassWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

@Composable
private fun ShapeSelectionCard(
    shape: CompassDialShape,
    shapeName: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onSelect)
            .testTag("shape_option_${shapeName.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Clean, uncluttered thumbnail preview of the exact dial shape (without direction letters)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f)
                    .padding(vertical = 4.dp)
            ) {
                Surface(
                    shape = shape.shape,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        }
                    ),
                    tonalElevation = if (isSelected) 3.dp else 1.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Minimal stylized center compass indicator pointing North
                        val indicatorColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }

                        Canvas(modifier = Modifier.size(width = 12.dp, height = 16.dp)) {
                            val w = size.width
                            val h = size.height
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(w / 2f, 0f)
                                lineTo(w, h)
                                lineTo(w / 2f, h * 0.78f)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = indicatorColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = shapeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}
