package me.samud.minimumlauncher

sealed class ListItem {
    data class AppItem(val appInfo: AppInfo) : ListItem()
    data class HeaderItem(val title: String) : ListItem()
}
