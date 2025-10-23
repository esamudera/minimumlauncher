package me.samud.minimumlauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class PinShortcutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PinShortcutActivity", "PinShortcutActivity called")

        if (intent?.action == "android.content.pm.action.CONFIRM_PIN_SHORTCUT") {
            handlePinShortcut()
        }

        finish()
    }

    private fun handlePinShortcut() {
        Log.d("PinShortcutActivity", "intent received")
        val pinItemRequest = intent.getParcelableExtra<LauncherApps.PinItemRequest>(LauncherApps.EXTRA_PIN_ITEM_REQUEST)
        if (pinItemRequest == null) {
            Log.d("PinShortcutActivity", "PinItemRequest is null")
            return
        }

        Log.d("PinShortcutActivity", "PinItemRequest valid: ${pinItemRequest.isValid}")
        Log.d("PinShortcutActivity", "PinItemRequest type: ${pinItemRequest.requestType}")

        if (pinItemRequest.requestType == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) {
            val shortcutInfo = pinItemRequest.shortcutInfo
            if (shortcutInfo != null) {
                Log.d("PinShortcutActivity", "ShortcutInfo: $shortcutInfo")
                // Save the shortcut
                val shortcutManager = ShortcutManager(this)
                shortcutManager.addShortcut(shortcutInfo)

                // Accept the request to let the system know it was handled
                pinItemRequest.accept()

                Toast.makeText(this, "Shortcut pinned!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
