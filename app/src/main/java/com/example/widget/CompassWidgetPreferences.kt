package com.example.widget

import android.content.Context
import com.example.ui.compass.CompassDialShape

/**
 * Independent persistence store for home-screen Compass widget configurations.
 * Each widget instance (identified by its appWidgetId) stores its own selected dial shape,
 * completely isolated from other widget instances and from the main app preference.
 */
object CompassWidgetPreferences {
    private const val PREFS_NAME = "compass_widget_preferences"
    private const val PREFIX_SHAPE = "widget_shape_"

    fun getWidgetShape(context: Context, appWidgetId: Int): CompassDialShape {
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val shapeName = sp.getString("$PREFIX_SHAPE$appWidgetId", "Sunny")
        return when (shapeName) {
            "Circle" -> CompassDialShape.Circle
            "Diamond" -> CompassDialShape.Diamond
            "Octagon" -> CompassDialShape.Octagon
            else -> CompassDialShape.Sunny
        }
    }

    fun setWidgetShape(context: Context, appWidgetId: Int, shape: CompassDialShape) {
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val shapeName = when (shape) {
            CompassDialShape.Sunny -> "Sunny"
            CompassDialShape.Circle -> "Circle"
            CompassDialShape.Diamond -> "Diamond"
            CompassDialShape.Octagon -> "Octagon"
        }
        sp.edit().putString("$PREFIX_SHAPE$appWidgetId", shapeName).apply()
    }

    fun deleteWidgetShape(context: Context, appWidgetId: Int) {
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().remove("$PREFIX_SHAPE$appWidgetId").apply()
    }
}
