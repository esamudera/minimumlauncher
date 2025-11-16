package me.samud.minimumlauncher

import android.content.Context
import android.content.pm.ShortcutInfo
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class FavoritesManager(context: Context) {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val favoriteAppsKey = "favorite_apps"
    private val favoriteShortcutsKey = "favorite_shortcuts"

    private val _favoritesChanged = MutableSharedFlow<Unit>(replay = 1)
    val favoritesChanged = _favoritesChanged.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        notifyChanges()
    }

    private fun notifyChanges() {
        scope.launch {
            _favoritesChanged.emit(Unit)
        }
    }

    // App Favorites
    fun getFavoriteApps(): MutableSet<String> {
        return prefs.getStringSet(favoriteAppsKey, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    fun addFavoriteApp(packageName: String) {
        val favorites = getFavoriteApps()
        favorites.add(packageName)
        prefs.edit { putStringSet(favoriteAppsKey, favorites) }
        notifyChanges()
    }

    fun removeFavoriteApp(packageName: String) {
        val favorites = getFavoriteApps()
        favorites.remove(packageName)
        prefs.edit { putStringSet(favoriteAppsKey, favorites) }
        notifyChanges()
    }

    fun isFavoriteApp(packageName: String): Boolean {
        return getFavoriteApps().contains(packageName)
    }

    // Shortcut Favorites
    fun getFavoriteShortcuts(): MutableSet<String> {
        return prefs.getStringSet(favoriteShortcutsKey, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun getShortcutId(shortcutInfo: ShortcutInfo): String {
        return "${shortcutInfo.`package`}/${shortcutInfo.id}"
    }

    fun addFavoriteShortcut(shortcutInfo: ShortcutInfo) {
        val favorites = getFavoriteShortcuts()
        favorites.add(getShortcutId(shortcutInfo))
        prefs.edit { putStringSet(favoriteShortcutsKey, favorites) }
        notifyChanges()
    }

    fun removeFavoriteShortcut(shortcutInfo: ShortcutInfo) {
        val favorites = getFavoriteShortcuts()
        favorites.remove(getShortcutId(shortcutInfo))
        prefs.edit { putStringSet(favoriteShortcutsKey, favorites) }
        notifyChanges()
    }

    fun isFavoriteShortcut(shortcutInfo: ShortcutInfo): Boolean {
        return getFavoriteShortcuts().contains(getShortcutId(shortcutInfo))
    }
}