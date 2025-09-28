package me.samud.minimumlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

/**
 * Concrete implementation of AppSource that uses the Android PackageManager.
 */
class PackageManagerAppSource(private val packageManager: PackageManager) : AppSource {

    override fun getLaunchableApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val allAppsResolveInfo: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)

        val apps = mutableListOf<AppInfo>()
        for (resolveInfo in allAppsResolveInfo) {
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val packageName = resolveInfo.activityInfo.packageName
            apps.add(AppInfo(appName, packageName))
        }
        return apps
    }
}
