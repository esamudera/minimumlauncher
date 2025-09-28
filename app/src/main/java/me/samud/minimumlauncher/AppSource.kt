package me.samud.minimumlauncher

/**
 * Interface for abstracting the source of application information.
 */
interface AppSource {
    fun getLaunchableApps(): List<AppInfo>
}
