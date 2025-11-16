package me.samud.minimumlauncher

import android.content.Context
import android.content.pm.ShortcutInfo
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog

class ShortcutBottomSheetDialog(
    private val context: Context,
    private val shortcutInfo: ShortcutInfo
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

        if (favoritesManager.isFavoriteShortcut(shortcutInfo)) {
            menuItems.add(BottomSheetMenuItem(context.getString(R.string.action_remove_from_favorite), android.R.drawable.ic_menu_delete) {
                favoritesManager.removeFavoriteShortcut(shortcutInfo)
                bottomSheetDialog.dismiss()
            })
        } else {
            menuItems.add(BottomSheetMenuItem(context.getString(R.string.action_add_to_favorite), android.R.drawable.ic_menu_add) {
                favoritesManager.addFavoriteShortcut(shortcutInfo)
                bottomSheetDialog.dismiss()
            })
        }

        // TODO: Add other shortcut-related options here if needed.
        // For example, "Pin shortcut" is already handled by the system, but other actions could be added.

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
