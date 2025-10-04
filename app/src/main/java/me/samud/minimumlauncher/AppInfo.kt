package me.samud.minimumlauncher

data class AppInfo(
    val name: String,
    val packageName: String, // Added package name for launching functionality
    var isFavorite: Boolean = false
)