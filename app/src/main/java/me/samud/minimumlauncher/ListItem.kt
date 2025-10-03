package me.samud.minimumlauncher

import android.content.Context
import androidx.fragment.app.FragmentManager

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

import android.widget.Toast


sealed class ListItem {
    interface Launchable {
        fun onLaunch(context: Context, fragmentManager: FragmentManager?)
    }

    interface LongClickable {
        fun onLongClick(context: Context): Boolean
    }

    data class UserAppItem(val appInfo: AppInfo) : ListItem(), Launchable, LongClickable {
        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
            launchIntent?.let {
                context.startActivity(it)
            }
        }

        override fun onLongClick(context: Context): Boolean {
            Toast.makeText(context, "Long clicked: ${appInfo.name}", Toast.LENGTH_SHORT).show()
            return true
        }
    }

    data class InternalFragmentItem(val title: String, val destination: KClass<out Fragment>) : ListItem(),
        Launchable {
        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            fragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, destination.java.newInstance())
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    data class WebShortcutItem(val title: String, val url: String) : ListItem(), Launchable, LongClickable {
        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            // TODO: Not yet implemented
        }

        override fun onLongClick(context: Context): Boolean {
            Toast.makeText(context, "Long clicked: $title", Toast.LENGTH_SHORT).show()
            return true
        }
    }

    data class HeaderItem(val title: String) : ListItem()
}
