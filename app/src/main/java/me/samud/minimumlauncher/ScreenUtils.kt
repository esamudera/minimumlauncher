package me.samud.minimumlauncher

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowMetrics

/**
 * Returns the height of the screen in pixels.
 * This function handles API compatibility by using WindowMetrics on API 30+ and DisplayMetrics on older versions.
 *
 * @return The screen height in pixels.
 */
fun Activity.getScreenHeight(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
        windowMetrics.bounds.height()
    } else {
        @Suppress("DEPRECATION")
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}
