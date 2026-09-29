package com.example.widget

import android.app.PendingIntent
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.sensor.CompassState
import com.example.sensor.OrientationSensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Centralized lifecycle and sensor management for live home-screen Compass widget rotation
 * and real-time Dynamic Color palette refresh.
 *
 * Automatically monitors device wallpaper and system Monet color changes to update
 * existing placed widget instances immediately without requiring re-addition,
 * while managing sensors responsibly when screen is OFF or no widgets exist.
 */
object CompassWidgetUpdateManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var sensorJob: Job? = null
    private var isReceiverRegistered = false
    private var isWallpaperListenerRegistered = false
    private var wallpaperColorsListener: Any? = null

    private var lastRenderTimeMs = 0L
    private var lastRenderedHeading = -1f
    private var lastKnownState: CompassState? = null

    // ~30 FPS throttle threshold for responsive yet battery-conscious home-screen rendering
    private const val MIN_FRAME_INTERVAL_MS = 33L

    private val systemEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val appContext = context.applicationContext
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    stopSensorStream()
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    startSensorStreamIfWidgetsExist(appContext)
                }
                Intent.ACTION_WALLPAPER_CHANGED,
                Intent.ACTION_CONFIGURATION_CHANGED,
                "android.intent.action.OVERLAY_CHANGED",
                Intent.ACTION_LOCALE_CHANGED -> {
                    // System wallpaper or dynamic color overlay changed: update all placed widgets immediately
                    updateAllWidgets(appContext)
                }
            }
        }
    }

    @Synchronized
    fun startSensorStreamIfWidgetsExist(context: Context) {
        val appContext = context.applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val providerComponent = ComponentName(appContext, CompassWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(providerComponent)

        if (widgetIds.isEmpty()) {
            stopSensorStream()
            return
        }

        // 1. Register system event receiver once (Screen ON/OFF + Wallpaper & Configuration changes)
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_WALLPAPER_CHANGED)
                addAction(Intent.ACTION_CONFIGURATION_CHANGED)
                addAction("android.intent.action.OVERLAY_CHANGED")
                addAction(Intent.ACTION_LOCALE_CHANGED)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appContext.registerReceiver(systemEventReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    appContext.registerReceiver(systemEventReceiver, filter)
                }
                isReceiverRegistered = true
            } catch (_: Exception) { }
        }

        // 2. Register WallpaperColors listener for real-time Material You palette adaptation on Android 8.1+ / 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !isWallpaperListenerRegistered) {
            try {
                val wallpaperManager = appContext.getSystemService(Context.WALLPAPER_SERVICE) as? WallpaperManager
                val listener = WallpaperManager.OnColorsChangedListener { _, _ ->
                    updateAllWidgets(appContext)
                }
                wallpaperManager?.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
                wallpaperColorsListener = listener
                isWallpaperListenerRegistered = true
            } catch (_: Exception) { }
        }

        // 3. Check if screen is currently on and interactive
        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isScreenOn = powerManager?.isInteractive ?: true
        if (!isScreenOn) {
            stopSensorStream()
            return
        }

        if (sensorJob?.isActive == true) {
            return
        }

        val sensorManager = OrientationSensorManager(appContext)
        sensorJob = scope.launch {
            sensorManager.getOrientationFlow().collectLatest { state ->
                lastKnownState = state
                onHeadingStateChanged(appContext, state)
            }
        }
    }

    @Synchronized
    fun stopSensorStream() {
        sensorJob?.cancel()
        sensorJob = null
    }

    private fun onHeadingStateChanged(context: Context, state: CompassState) {
        val now = System.currentTimeMillis()
        val heading = state.heading
        val headingDelta = abs((heading - lastRenderedHeading + 540f) % 360f - 180f)

        // Throttle updates: permit if interval has elapsed or noticeable heading change occurs
        if (now - lastRenderTimeMs < MIN_FRAME_INTERVAL_MS && headingDelta < 0.25f) {
            return
        }

        lastRenderTimeMs = now
        lastRenderedHeading = heading

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val providerComponent = ComponentName(context, CompassWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(providerComponent)

        if (widgetIds.isEmpty()) {
            stopSensorStream()
            return
        }

        for (widgetId in widgetIds) {
            updateSingleWidget(context, appWidgetManager, widgetId, state)
        }
    }

    fun updateSingleWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        state: CompassState? = null
    ) {
        val activeState = state ?: lastKnownState
        val shape = CompassWidgetPreferences.getWidgetShape(context, widgetId)
        val heading = activeState?.heading ?: 0f
        val direction = activeState?.direction ?: "N"

        val bitmap = CompassWidgetRenderer.renderDialBitmap(
            context = context,
            shape = shape,
            rotationDegrees = heading,
            headingDegrees = heading,
            currentDirection = direction
        )

        val views = RemoteViews(context.packageName, R.layout.widget_compass_layout)
        views.setImageViewBitmap(R.id.widget_dial_image, bitmap)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            widgetId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    fun updateAllWidgets(context: Context) {
        val appContext = context.applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val providerComponent = ComponentName(appContext, CompassWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(providerComponent)

        // Force clear throttle state so that fresh dynamic colors are pushed immediately
        lastRenderTimeMs = 0L
        lastRenderedHeading = -1f

        for (widgetId in widgetIds) {
            updateSingleWidget(appContext, appWidgetManager, widgetId, lastKnownState)
        }
        startSensorStreamIfWidgetsExist(appContext)
    }
}
