package me.samud.minimumlauncher

// Removed unused imports: Intent, PackageManager, ResolveInfo

class AppLoader(private val appSource: AppSource) { // Changed dependency to AppSource

    fun loadAndSortApps(): List<AppInfo> {
        val apps = appSource.getLaunchableApps().toMutableList() // Get apps from AppSource

        // Sort apps alphabetically
        apps.sortBy { it.name.lowercase() }

        return apps
    }
}
