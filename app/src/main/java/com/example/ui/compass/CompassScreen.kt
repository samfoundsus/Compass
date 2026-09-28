package com.example.ui.compass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExploreOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.theme.CompassThemeTokens

/**
 * Main Compass screen displaying the top bar and real-time orientation-driven compass dial
 * in a calm, spacious, and balanced Pixel aesthetic.
 */
@Composable
fun CompassScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompassViewModel = viewModel()
) {
    val spacing = CompassThemeTokens.spacing
    val compassState by viewModel.uiState.collectAsStateWithLifecycle()

    // UI-only lock state
    var isLocked by remember { mutableStateOf(false) }
    var lockedHeading by remember { mutableFloatStateOf(0f) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar Area (blends seamlessly with screen background, buttons spaced toward right)
            CompassTopBar(
                isLocked = isLocked,
                onToggleLock = {
                    if (!isLocked) {
                        lockedHeading = compassState.heading
                        isLocked = true
                    } else {
                        isLocked = false
                    }
                },
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = spacing.large, end = 8.dp, top = spacing.small, bottom = spacing.small)
            )

            // Central Area containing the centered Compass Dial
            BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = spacing.medium)
            ) {
                // Responsive calculation for dial diameter leaving generous whitespace
                val dialSize = min(maxWidth * 0.88f, maxHeight * 0.74f).coerceIn(260.dp, 380.dp)

                if (!compassState.isSensorAvailable) {
                    // Graceful fallback for devices without orientation sensors
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = spacing.large)
                            .testTag("sensor_unavailable_container")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ExploreOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.compass_sensor_unavailable),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.compass_sensor_unavailable_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Real-time sensor-driven Compass Dial (with rotation locking support)
                    val activeRotation = if (isLocked) lockedHeading else compassState.heading

                    CompassDial(
                        shape = CompassDialSettings.currentShape,
                        rotationDegrees = activeRotation,
                        headingDegrees = compassState.heading,
                        currentDirection = compassState.direction,
                        modifier = Modifier.size(dialSize)
                    )
                }
            }
        }
    }
}

/**
 * Top area with large prominent title on the left and action buttons on the right.
 */
@Composable
private fun CompassTopBar(
    isLocked: Boolean,
    onToggleLock: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = CompassThemeTokens.spacing

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        // Large Prominent Title
        Text(
            text = stringResource(id = R.string.title_compass),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("title_compass")
        )

        // Top Right Actions: Lock & Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
        ) {
            IconButton(
                onClick = onToggleLock,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("lock_button")
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                    contentDescription = stringResource(
                        id = if (isLocked) R.string.action_unlock_heading else R.string.action_lock_heading
                    ),
                    tint = if (isLocked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(id = R.string.action_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
