package me.samud.minimumlauncher

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent

class NotificationExpansionService : AccessibilityService() {

    companion object {
        // A static reference to the service instance for easy access
        // This is a simple approach; in a complex app, you might use binding or broadcast
        private var instance: NotificationExpansionService? = null

        fun triggerNotificationExpansion() {
            instance?.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not needed for this feature
    }

    override fun onInterrupt() {
        // Not needed for this feature
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        
        // You can configure the service here, but it's often
        // done in the XML file (see step 2)
        val info = AccessibilityServiceInfo()
        info.eventTypes = 0 // We don't need to listen to events
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
