package com.example.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.CompassPreferences
import com.example.sensor.LocationDeclinationManager
import com.example.sensor.NorthReference
import com.example.sensor.SmoothingMode
import com.example.ui.compass.CompassDialSettings
import com.example.ui.compass.CompassDialShape
import com.example.ui.compass.CompassViewModel
import com.example.ui.theme.AppThemeState
import com.example.ui.theme.CompassThemeTokens
import com.example.ui.theme.ThemeMode

/**
 * Pixel/Google-style clean, typography-focused settings screen with grouped rounded containers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompassViewModel = viewModel()
) {
    BackHandler(onBack = onNavigateBack)
    val context = LocalContext.current
    val spacing = CompassThemeTokens.spacing
    val compassState by viewModel.uiState.collectAsStateWithLifecycle()

    // Interactive dialog states
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDialShapeDialog by remember { mutableStateOf(false) }
    var showNorthRefDialog by remember { mutableStateOf(false) }
    var showSmoothingDialog by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var showPermissionExplanationDialog by remember { mutableStateOf(false) }

    // Permission launcher for Location (needed for True North & Magnetic Declination)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            CompassPreferences.setNorthReferenceMode(NorthReference.TRUE_NORTH, context)
            CompassPreferences.updateDeclinationFromLocation(context)
        } else {
            // Permission denied (normal, repeated, or permanent)
            // Immediately switch back to Magnetic North, persist, and update UI
            CompassPreferences.setNorthReferenceMode(NorthReference.MAGNETIC, context)
            CompassPreferences.updateDeclinationFromLocation(context)
        }
    }

    // 1. Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = AppThemeState.themeMode,
            onThemeSelected = { mode ->
                CompassPreferences.setThemeMode(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // 2. Dial Shape Selection Dialog
    if (showDialShapeDialog) {
        DialShapeSelectionDialog(
            currentShape = CompassDialSettings.currentShape,
            onShapeSelected = { shape ->
                CompassPreferences.setDialShape(shape)
                showDialShapeDialog = false
            },
            onDismiss = { showDialShapeDialog = false }
        )
    }

    // 3. North Reference Selection Dialog
    if (showNorthRefDialog) {
        NorthReferenceDialog(
            currentRef = CompassPreferences.northReference,
            onRefSelected = { ref ->
                showNorthRefDialog = false
                if (ref == NorthReference.TRUE_NORTH) {
                    if (LocationDeclinationManager.hasLocationPermission(context)) {
                        CompassPreferences.setNorthReferenceMode(NorthReference.TRUE_NORTH, context)
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                } else {
                    CompassPreferences.setNorthReferenceMode(NorthReference.MAGNETIC, context)
                }
            },
            onDismiss = { showNorthRefDialog = false }
        )
    }

    // 4. Sensor Smoothing Selection Dialog
    if (showSmoothingDialog) {
        SmoothingSelectionDialog(
            currentMode = CompassPreferences.smoothingMode,
            onModeSelected = { mode ->
                CompassPreferences.setSmoothing(mode)
                showSmoothingDialog = false
            },
            onDismiss = { showSmoothingDialog = false }
        )
    }

    // 5. Compass Calibration Dialog
    if (showCalibrationDialog) {
        CalibrationDialog(
            accuracy = compassState.accuracy,
            fieldStrength = compassState.fieldStrengthMicroTesla,
            onDismiss = { showCalibrationDialog = false }
        )
    }

    // 6. Location Permission Explanation Dialog
    if (showPermissionExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionExplanationDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.location_permission_dialog_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.location_permission_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionExplanationDialog = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.testTag("location_permission_confirm")
                ) {
                    Text(text = stringResource(id = R.string.permission_grant))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionExplanationDialog = false },
                    modifier = Modifier.testTag("location_permission_dismiss")
                ) {
                    Text(text = stringResource(id = R.string.permission_cancel))
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                // Prominent Settings Header
                Text(
                    text = stringResource(id = R.string.title_settings),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = spacing.medium)
                        .testTag("settings_screen_title")
                )

                Spacer(modifier = Modifier.height(spacing.small))

                // ==========================================
                // SECTION 1: APPEARANCE
                // ==========================================
                SettingsSectionHeader(title = stringResource(id = R.string.settings_category_appearance))
                SettingsGroupContainer {
                    val themeDesc = when (AppThemeState.themeMode) {
                        ThemeMode.SYSTEM -> stringResource(id = R.string.theme_system)
                        ThemeMode.LIGHT -> stringResource(id = R.string.theme_light)
                        ThemeMode.DARK -> stringResource(id = R.string.theme_dark)
                    }
                    SettingsRow(
                        title = stringResource(id = R.string.settings_theme_title),
                        description = themeDesc,
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("setting_theme")
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        title = stringResource(id = R.string.settings_dynamic_color_title),
                        description = stringResource(id = R.string.settings_dynamic_color_desc),
                        checked = AppThemeState.dynamicColorEnabled,
                        onCheckedChange = { CompassPreferences.setDynamicColor(it) },
                        modifier = Modifier.testTag("setting_dynamic_color")
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // ==========================================
                // SECTION 2: COMPASS
                // ==========================================
                SettingsSectionHeader(title = stringResource(id = R.string.settings_category_compass))
                SettingsGroupContainer {
                    // North Reference
                    val northRefDesc = when (CompassPreferences.northReference) {
                        NorthReference.MAGNETIC -> stringResource(id = R.string.settings_north_ref_magnetic)
                        NorthReference.TRUE_NORTH -> stringResource(id = R.string.settings_north_ref_true)
                    }
                    SettingsRow(
                        title = stringResource(id = R.string.settings_north_ref_title),
                        description = northRefDesc,
                        onClick = { showNorthRefDialog = true },
                        modifier = Modifier.testTag("setting_north_reference")
                    )

                    SettingsDivider()

                    // Magnetic Declination
                    SettingsRow(
                        title = stringResource(id = R.string.settings_declination_title),
                        description = CompassPreferences.declinationStatus,
                        onClick = {
                            if (!LocationDeclinationManager.hasLocationPermission(context)) {
                                showPermissionExplanationDialog = true
                            } else {
                                CompassPreferences.updateDeclinationFromLocation(context)
                            }
                        },
                        modifier = Modifier.testTag("setting_magnetic_declination")
                    )

                    SettingsDivider()

                    // Sensor Smoothing
                    val smoothingDesc = when (CompassPreferences.smoothingMode) {
                        SmoothingMode.ADAPTIVE -> stringResource(id = R.string.settings_smoothing_adaptive_desc)
                        SmoothingMode.HIGH_STABILITY -> stringResource(id = R.string.settings_smoothing_high_desc)
                        SmoothingMode.RESPONSIVE -> stringResource(id = R.string.settings_smoothing_low_desc)
                    }
                    SettingsRow(
                        title = stringResource(id = R.string.settings_smoothing_title),
                        description = smoothingDesc,
                        onClick = { showSmoothingDialog = true },
                        modifier = Modifier.testTag("setting_sensor_smoothing")
                    )

                    SettingsDivider()

                    // Haptic Feedback
                    SettingsSwitchRow(
                        title = stringResource(id = R.string.settings_haptics_title),
                        description = stringResource(id = R.string.settings_haptics_desc),
                        checked = CompassPreferences.hapticsEnabled,
                        onCheckedChange = { CompassPreferences.setHaptics(it) },
                        modifier = Modifier.testTag("setting_haptic_feedback")
                    )

                    SettingsDivider()

                    // Dial Shape
                    SettingsRow(
                        title = stringResource(id = R.string.settings_dial_shape_title),
                        description = stringResource(id = CompassDialSettings.currentShape.nameRes),
                        onClick = { showDialShapeDialog = true },
                        modifier = Modifier.testTag("setting_dial_shape")
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // ==========================================
                // SECTION 3: CALIBRATION
                // ==========================================
                SettingsSectionHeader(title = stringResource(id = R.string.settings_category_calibration))
                SettingsGroupContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag("setting_calibration_group")
                    ) {
                        Text(
                            text = stringResource(id = R.string.settings_calibration_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.settings_calibration_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FilledTonalButton(
                            onClick = { showCalibrationDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.testTag("calibrate_button")
                        ) {
                            Text(text = stringResource(id = R.string.action_calibrate))
                        }
                    }
                }
            }
        }
    }
}

/**
 * North Reference Selection Dialog (Magnetic North vs True North).
 */
@Composable
private fun NorthReferenceDialog(
    currentRef: NorthReference,
    onRefSelected: (NorthReference) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        NorthReference.MAGNETIC to stringResource(id = R.string.settings_north_ref_magnetic),
        NorthReference.TRUE_NORTH to stringResource(id = R.string.settings_north_ref_true)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.settings_north_ref_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                options.forEach { (ref, label) ->
                    val isSelected = ref == currentRef
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { onRefSelected(ref) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                            .testTag("north_ref_${ref.name.lowercase()}")
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("north_ref_cancel")
            ) {
                Text(text = "Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

/**
 * Sensor Smoothing Selection Dialog.
 */
@Composable
private fun SmoothingSelectionDialog(
    currentMode: SmoothingMode,
    onModeSelected: (SmoothingMode) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        SmoothingMode.ADAPTIVE to stringResource(id = R.string.settings_smoothing_adaptive_desc),
        SmoothingMode.HIGH_STABILITY to stringResource(id = R.string.settings_smoothing_high_desc),
        SmoothingMode.RESPONSIVE to stringResource(id = R.string.settings_smoothing_low_desc)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.settings_smoothing_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                options.forEach { (mode, label) ->
                    val isSelected = mode == currentMode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { onModeSelected(mode) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                            .testTag("smoothing_${mode.name.lowercase()}")
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("smoothing_cancel")
            ) {
                Text(text = "Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

/**
 * Full Material 3 Compass Calibration Dialog with figure-8 guide and live sensor readout.
 */
@Composable
private fun CalibrationDialog(
    accuracy: Int,
    fieldStrength: Float,
    onDismiss: () -> Unit
) {
    val accuracyText = when (accuracy) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> stringResource(id = R.string.calibration_accuracy_high)
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> stringResource(id = R.string.calibration_accuracy_medium)
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> stringResource(id = R.string.calibration_accuracy_low)
        else -> stringResource(id = R.string.calibration_accuracy_unreliable)
    }

    val badgeColor = when (accuracy) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> MaterialTheme.colorScheme.primaryContainer
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }

    val badgeTextColor = when (accuracy) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> MaterialTheme.colorScheme.onPrimaryContainer
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.calibration_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Accuracy Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = badgeColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = accuracyText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Field strength readout
                Text(
                    text = stringResource(id = R.string.calibration_field_strength, fieldStrength),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Instruction
                Text(
                    text = stringResource(id = R.string.calibration_instruction),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.calibration_field_normal),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("calibration_done_button")
            ) {
                Text(text = stringResource(id = R.string.calibration_done))
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

/**
 * Clean Material 3 single-choice selection dialog for Theme Mode.
 */
@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        ThemeMode.SYSTEM to stringResource(id = R.string.theme_system),
        ThemeMode.LIGHT to stringResource(id = R.string.theme_light),
        ThemeMode.DARK to stringResource(id = R.string.theme_dark)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.settings_theme_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                options.forEach { (mode, label) ->
                    val isSelected = mode == currentTheme
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { onThemeSelected(mode) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                            .testTag("theme_option_${mode.name.lowercase()}")
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("theme_dialog_dismiss_button")
            ) {
                Text(text = "Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

/**
 * Clean Material 3 single-choice selection dialog for Dial Shapes.
 */
@Composable
private fun DialShapeSelectionDialog(
    currentShape: CompassDialShape,
    onShapeSelected: (CompassDialShape) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.settings_dial_shape_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                CompassDialSettings.availableShapes.forEach { shape ->
                    val isSelected = shape == currentShape
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { onShapeSelected(shape) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                            .testTag("dial_shape_option_${stringResource(id = shape.nameRes).lowercase()}")
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(id = shape.nameRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_dismiss_button")
            ) {
                Text(text = "Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

/**
 * Section label sitting above the grouped rounded container.
 */
@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(
            start = 24.dp,
            end = 24.dp,
            bottom = 8.dp
        )
    )
}

/**
 * Modern M3 rounded container grouping settings rows for a section.
 */
@Composable
private fun SettingsGroupContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

/**
 * Subtle divider between items within a rounded container.
 */
@Composable
private fun SettingsDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        thickness = 1.dp
    )
}

/**
 * Clean typography-focused 2-level settings row with natural text wrapping.
 */
@Composable
private fun SettingsRow(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

/**
 * Clean typography-focused settings row containing a toggle switch.
 */
@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag("switch_${title.lowercase().replace(" ", "_")}")
        )
    }
}
