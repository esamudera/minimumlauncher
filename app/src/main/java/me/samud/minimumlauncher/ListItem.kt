package me.samud.minimumlauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.reflect.KClass


sealed class ListItem {
    interface Launchable {
        fun onLaunch(context: Context, fragmentManager: FragmentManager?)
    }

    interface LongClickable {
        fun onLongClick(context: Context, fragmentManager: FragmentManager?): Boolean
    }

    data class UserAppItem(val appInfo: AppInfo) : ListItem(), Launchable, LongClickable {
        override fun onLaunch(context: Context, fragmentManager: FragmentManager?) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
            launchIntent?.let {
                context.startActivity(it)
            }
        }

        override fun onLongClick(context: Context, fragmentManager: FragmentManager?): Boolean {
            val bottomSheetDialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_menu, null)
            val menuLayout = view.findViewById<LinearLayout>(R.id.menu_layout)

            // "App info" item
            val appInfoItem = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_menu_item, menuLayout, false) as LinearLayout
            val iconView = appInfoItem.findViewById<ImageView>(R.id.icon)
            val textView = appInfoItem.findViewById<TextView>(R.id.text)
            
            textView.text = "App info"
            // Use Material Design icon for app info (ic_info_outline)
            // Note: You might need to add this icon to your resources or use Android system icon
            iconView.setImageResource(android.R.drawable.ic_dialog_info)
            
            appInfoItem.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", appInfo.packageName, null)
                context.startActivity(intent)
                bottomSheetDialog.dismiss()
            }
            menuLayout.addView(appInfoItem)

            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
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

        override fun onLongClick(context: Context, fragmentManager: FragmentManager?): Boolean {
            // TODO: Not yet implemented
            return true
        }
    }

    data class HeaderItem(val title: String) : ListItem()
}
