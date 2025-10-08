package me.samud.minimumlauncher

import android.content.Context
import android.content.Intent
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

    data class WebShortcutItem(val title: String, val url: String) : ListItem(), Launchable, LongClickable {
        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            // TODO: Not yet implemented
        }

        override fun onLongClick(context: Context, fragmentManager: FragmentManager?, sharedViewModel: SharedViewModel): Boolean {
            // TODO: Not yet implemented
            return true
        }
    }

    data class HeaderItem(val title: String) : ListItem()

    data class WidgetItem(val height: Int) : ListItem()
}
