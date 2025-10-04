package me.samud.minimumlauncher

import android.content.Context

class FavoritesManager(context: Context) {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val favoritesKey = "favorite_apps"

    fun getFavorites(): MutableSet<String> {
        return prefs.getStringSet(favoritesKey, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    fun addFavorite(packageName: String) {
        val favorites = getFavorites()
        favorites.add(packageName)
        prefs.edit().putStringSet(favoritesKey, favorites).apply()
    }

    fun removeFavorite(packageName: String) {
        val favorites = getFavorites()
        favorites.remove(packageName)
        prefs.edit().putStringSet(favoritesKey, favorites).apply()
    }

    fun isFavorite(packageName: String): Boolean {
        return getFavorites().contains(packageName)
    }
}
