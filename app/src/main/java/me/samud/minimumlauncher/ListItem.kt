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
    interface Launchable {
        fun onLaunch(context: Context, fragmentManager: FragmentManager?)
    }

    interface LongClickable {
        fun onLongClick(context: Context, fragmentManager: FragmentManager?, sharedViewModel: SharedViewModel): Boolean
    }

    data class UserAppItem(val appInfo: AppInfo) : ListItem(), Launchable, LongClickable {
        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
            launchIntent?.let {
                context.startActivity(it)
            }
        }

        override fun onLongClick(context: Context, fragmentManager: FragmentManager?, sharedViewModel: SharedViewModel): Boolean {
            UserAppBottomSheetDialog(context, appInfo, sharedViewModel).show()
            return true
        }
    }

    data class InternalActivityItem(val title: String, val intent: Intent) : ListItem(), Launchable {
        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            context.startActivity(intent)
        }
    }

    data class ShortcutItem(val shortcutInfo: android.content.pm.ShortcutInfo) : ListItem(), Launchable, LongClickable {
        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            launcherApps.startShortcut(shortcutInfo, null, null)
        }

        override fun onLongClick(context: Context, fragmentManager: FragmentManager?, sharedViewModel: SharedViewModel): Boolean {
            ShortcutBottomSheetDialog(context, shortcutInfo, sharedViewModel).show()
            return true
        }
    }

    data class HeaderItem(val title: String) : ListItem()

    data class WidgetItem(val height: Int) : ListItem()

    data class EmptyStateItem(val messageResId: Int) : ListItem()
}
