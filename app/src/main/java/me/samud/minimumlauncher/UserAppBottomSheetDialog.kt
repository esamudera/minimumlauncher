package me.samud.minimumlauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog

class UserAppBottomSheetDialog(
    private val context: Context,
    private val appInfo: AppInfo
) {

    private data class BottomSheetMenuItem(
        val text: String,
        val iconResId: Int,
        val onClick: () -> Unit
    )

    fun show() {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_menu, null)
        val menuLayout = view.findViewById<LinearLayout>(R.id.menu_layout)

        getMenuItems(bottomSheetDialog).forEach { (text, iconResId, onClick) ->
            val menuItemView = createMenuItem(text, iconResId, onClick, menuLayout)
            menuLayout.addView(menuItemView)
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun getMenuItems(bottomSheetDialog: BottomSheetDialog): List<BottomSheetMenuItem> {
        val favoritesManager = FavoritesManager(context)
        val menuItems = mutableListOf<BottomSheetMenuItem>()

        if (favoritesManager.isFavoriteApp(appInfo.packageName)) {
            menuItems.add(BottomSheetMenuItem("Remove from favorite", android.R.drawable.ic_menu_delete) {
                favoritesManager.removeFavoriteApp(appInfo.packageName)
                bottomSheetDialog.dismiss()
            })
        } else {
            menuItems.add(BottomSheetMenuItem("Add to favorite", android.R.drawable.ic_menu_add) {
                favoritesManager.addFavoriteApp(appInfo.packageName)
                bottomSheetDialog.dismiss()
            })
        }

        menuItems.add(BottomSheetMenuItem("Uninstall", android.R.drawable.ic_menu_delete) {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.fromParts("package", appInfo.packageName, null)
            context.startActivity(intent)
            bottomSheetDialog.dismiss()
        })

        menuItems.add(BottomSheetMenuItem("Hide", android.R.drawable.ic_menu_close_clear_cancel) {
            // TODO: Implement hide functionality
            bottomSheetDialog.dismiss()
        })

        menuItems.add(BottomSheetMenuItem("App info", android.R.drawable.ic_dialog_info) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", appInfo.packageName, null)
            context.startActivity(intent)
            bottomSheetDialog.dismiss()
        })

        return menuItems
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