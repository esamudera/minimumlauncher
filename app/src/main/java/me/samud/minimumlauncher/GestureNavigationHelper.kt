package me.samud.minimumlauncher

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Insets
import android.os.Build
import android.view.View
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

    /**
     * Applies a fade-in transition to a view if gesture navigation is enabled.
     */
    fun applyGestureNavigationTransition(view: View, activity: Activity) {
        if (getNavigationMode(activity) != 0) { // Apply for gesture nav (1) and older OS (-1)
            view.alpha = 0f
            val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            fadeIn.duration = 50
            fadeIn.startDelay = 500
            fadeIn.start()
        }
    }

    /**
     * Prepares a view for a fade-in transition by setting its alpha to 0.
     */
    fun prepareTransition(view: View, activity: Activity) {
        if (getNavigationMode(activity) != 0) {
            view.alpha = 0f
        }
    }
}
