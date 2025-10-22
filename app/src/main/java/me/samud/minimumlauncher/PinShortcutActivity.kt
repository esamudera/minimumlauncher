package me.samud.minimumlauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
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
            Toast.makeText(this, "Shortcut feature will be implemented soon", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun handlePinShortcut() {
        Log.d("PinShortcutActivity", "intent received")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pinItemRequest = intent.getParcelableExtra<LauncherApps.PinItemRequest>(LauncherApps.EXTRA_PIN_ITEM_REQUEST)
            if (pinItemRequest == null) {
                Log.d("PinShortcutActivity", "PinItemRequest is null")
                return
            }

            Log.d("PinShortcutActivity", "PinItemRequest valid: ${pinItemRequest.isValid}")
            Log.d("PinShortcutActivity", "PinItemRequest type: ${pinItemRequest.requestType}")

            if (pinItemRequest.requestType == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) {
                val shortcutInfo = pinItemRequest.shortcutInfo
                Log.d("PinShortcutActivity", "ShortcutInfo: $shortcutInfo")
                if (shortcutInfo != null) {
                    Log.d("PinShortcutActivity", "  id: ${shortcutInfo.id}")
                    Log.d("PinShortcutActivity", "  package: ${shortcutInfo.getPackage()}")
                    Log.d("PinShortcutActivity", "  shortLabel: ${shortcutInfo.shortLabel}")
                    Log.d("PinShortcutActivity", "  longLabel: ${shortcutInfo.longLabel}")
                    Log.d("PinShortcutActivity", "  intent: ${shortcutInfo.intent}")
                }
            }
        }
    }
}
