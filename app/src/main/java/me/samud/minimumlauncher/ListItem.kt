package me.samud.minimumlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlin.reflect.KClass

sealed class ListItem {
    abstract val identifier: String

    open fun matches(query: String): Boolean = false

    interface Launchable {
        fun onLaunch(context: Context, fragmentManager: FragmentManager?)
    }

    interface LongClickable {
        fun onLongClick(context: Context, fragmentManager: FragmentManager?): Boolean
    }

    data class UserAppItem(val appInfo: AppInfo) : ListItem(), Launchable, LongClickable {
        override val identifier = appInfo.packageName

        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
            launchIntent?.let {
                context.startActivity(it)
            }
        }

        override fun onLongClick(context: Context, fragmentManager: FragmentManager?): Boolean {
            UserAppBottomSheetDialog(context, appInfo).show()
            return true
        }

        override fun matches(query: String): Boolean {
            return this.appInfo.name.contains(query, ignoreCase = true)
        }
    }

    data class InternalActivityItem(val title: String, val intent: Intent) : ListItem(), Launchable {
        override val identifier = "internal:$title"

        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            context.startActivity(intent)
        }

        override fun matches(query: String): Boolean {
            return this.title.contains(query, ignoreCase = true)
        }
    }

    data class ShortcutItem(val shortcutInfo: android.content.pm.ShortcutInfo) : ListItem(), Launchable, LongClickable {
        override val identifier = "${shortcutInfo.`package`}/${shortcutInfo.id}"

        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            launcherApps.startShortcut(shortcutInfo, null, null)
        }

        override fun onLongClick(context: Context, fragmentManager: FragmentManager?): Boolean {
            ShortcutBottomSheetDialog(context, shortcutInfo).show()
            return true
        }

        override fun matches(query: String): Boolean {
            return this.shortcutInfo.shortLabel?.toString()?.contains(query, ignoreCase = true) ?: false
        }
    }

    data class HeaderItem(val title: String) : ListItem() {
        override val identifier = "header:$title"
    }

    data class EmptyStateItem(val messageResId: Int) : ListItem() {
        override val identifier = "empty_state:$messageResId"
    }
}
