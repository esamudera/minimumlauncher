package me.samud.minimumlauncher

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog

class ShortcutBottomSheetDialog(
    private val context: Context,
    private val shortcutInfo: android.content.pm.ShortcutInfo,
    private val sharedViewModel: SharedViewModel
) : BottomSheetDialog(context) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_shortcut, null)
        setContentView(view)

        val removeButton = view.findViewById<LinearLayout>(R.id.remove_shortcut_layout)
        removeButton.setOnClickListener {
            val shortcutManager = ShortcutManager(context)
            shortcutManager.removeShortcut(shortcutInfo.`package`, shortcutInfo.id)
            // Re-use the favorites changed trigger to refresh the main list
            sharedViewModel.notifyFavoritesChanged()
            dismiss()
        }
    }
}
