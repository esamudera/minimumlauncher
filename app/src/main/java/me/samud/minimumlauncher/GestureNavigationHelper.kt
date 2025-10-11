package me.samud.minimumlauncher

import android.app.Activity
import android.graphics.Insets
import android.os.Build
import android.view.WindowInsets

object GestureNavigationHelper {

    /**
     * Determines the navigation mode.
     * @return 1 for gesture navigation, 0 for button navigation, -1 if below Android Q.
     */
    fun getNavigationMode(activity: Activity): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return -1
        }

        val rootInsets = activity.window?.decorView?.rootWindowInsets ?: return 0

        val insets: Insets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rootInsets.getInsets(WindowInsets.Type.systemGestures())
        } else {
            @Suppress("DEPRECATION")
            rootInsets.systemGestureInsets
        }

        return if (insets.left > 0 || insets.right > 0) 1 else 0
    }
}