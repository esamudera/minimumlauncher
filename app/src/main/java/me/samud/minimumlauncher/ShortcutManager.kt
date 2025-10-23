package me.samud.minimumlauncher

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process
import android.util.Log

class ShortcutManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("shortcuts", Context.MODE_PRIVATE)
    private val shortcutsKey = "pinned_shortcuts"
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    fun addShortcut(shortcutInfo: ShortcutInfo) {
        // Store a unique identifier for the shortcut, e.g., "packageName/id"
        val identifier = "${shortcutInfo.`package`}/${shortcutInfo.id}"
        val currentShortcuts = prefs.getStringSet(shortcutsKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentShortcuts.add(identifier)) {
            prefs.edit().putStringSet(shortcutsKey, currentShortcuts).apply()
            Log.d("ShortcutManager", "Added shortcut: $identifier")
        }
    }

    fun getShortcuts(): List<ShortcutInfo> {
        val shortcutIdentifiers = prefs.getStringSet(shortcutsKey, emptySet()) ?: emptySet()
        val shortcuts = mutableListOf<ShortcutInfo>()

        // Use LauncherApps to get the full ShortcutInfo for each stored identifier
        shortcutIdentifiers.forEach { identifier ->
            val parts = identifier.split('/', limit = 2)
            if (parts.size == 2) {
                val packageName = parts[0]
                val shortcutId = parts[1]
                val query = LauncherApps.ShortcutQuery().apply {
                    setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC)
                    setPackage(packageName)
                    setShortcutIds(listOf(shortcutId))
                }
                shortcuts.addAll(launcherApps.getShortcuts(query, Process.myUserHandle()) ?: emptyList())
            }
        }
        return shortcuts
    }

    fun removeShortcut(packageName: String, shortcutId: String) {
        val identifier = "$packageName/$shortcutId"
        val currentShortcuts = prefs.getStringSet(shortcutsKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentShortcuts.remove(identifier)) {
            prefs.edit().putStringSet(shortcutsKey, currentShortcuts).apply()
            Log.d("ShortcutManager", "Removed shortcut: $identifier")
        }
    }
}
