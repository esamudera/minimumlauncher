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


class UserAppBottomSheetDialog(private val context: Context, private val appInfo: AppInfo) {
    fun show() {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_menu, null)
        val menuLayout = view.findViewById<LinearLayout>(R.id.menu_layout)

        // Create a list of menu items to add
        val menuItems = listOf(
            Triple("Add to favorite", android.R.drawable.2ic_menu_add) {
                // TODO: Implement add to favorite functionality
                bottomSheetDialog.dismiss()
            },
            Triple("Uninstall", android.R.drawable.ic_menu_delete) {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.fromParts("package", appInfo.packageName, null)
                context.startActivity(intent)
                bottomSheetDialog.dismiss()
            },
            Triple("Hide", android.R.drawable.ic_menu_close_clear_cancel) {
                // TODO: Implement hide functionality
                bottomSheetDialog.dismiss()
            },
            Triple("App info", android.R.drawable.ic_dialog_info) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", appInfo.packageName, null)
                context.startActivity(intent)
                bottomSheetDialog.dismiss()
            }
        )

        // Add each menu item
        menuItems.forEach { (text, iconResId, onClick) ->
            val menuItem = createMenuItem(text, iconResId, onClick, menuLayout)
            menuLayout.addView(menuItem)
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun createMenuItem(
        text: String, 
        iconResId: Int, 
        onClick: () -> Unit,
        menuLayout: LinearLayout
    ): LinearLayout {
        val menuItem = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_menu_item, null, false) as LinearLayout
        val iconView = menuItem.findViewById<ImageView>(R.id.icon)
        val textView = menuItem.findViewById<TextView>(R.id.text)
        
        textView.text = text
        iconView.setImageResource(iconResId)
        
        menuItem.setOnClickListener { 
            onClick()
        }
        return menuItem
    }
}

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
            UserAppBottomSheetDialog(context, appInfo).show()
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
