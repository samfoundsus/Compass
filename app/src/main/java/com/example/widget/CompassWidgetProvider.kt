package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

/**
 * AppWidgetProvider for the native 2x2 home-screen Compass widget.
 * Renders the configured dial shape with live real-time sensor rotation,
 * handles theme and wallpaper changes, and opens the main Compass app on tap.
 */
class CompassWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            CompassWidgetUpdateManager.updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
        CompassWidgetUpdateManager.startSensorStreamIfWidgetsExist(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        CompassWidgetUpdateManager.startSensorStreamIfWidgetsExist(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        CompassWidgetUpdateManager.stopSensorStream()
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            CompassWidgetPreferences.deleteWidgetShape(context, appWidgetId)
        }
        CompassWidgetUpdateManager.startSensorStreamIfWidgetsExist(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Refresh all widget instances on configuration or wallpaper change
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED ||
            intent.action == Intent.ACTION_WALLPAPER_CHANGED
        ) {
            CompassWidgetUpdateManager.updateAllWidgets(context)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CompassWidgetUpdateManager.updateSingleWidget(context, appWidgetManager, appWidgetId)
            CompassWidgetUpdateManager.startSensorStreamIfWidgetsExist(context)
        }
    }
}
