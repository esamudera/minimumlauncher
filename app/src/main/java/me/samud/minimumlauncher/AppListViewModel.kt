package me.samud.minimumlauncher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    private val _items = MutableLiveData<List<ListItem>>()
    val items: LiveData<List<ListItem>> = _items

    private val _searchResults = MutableLiveData<List<ListItem>>()
    val searchResults: LiveData<List<ListItem>> = _searchResults

    // Dependencies are now created inside the ViewModel
    private val context: Context = getApplication<Application>().applicationContext
    private val appSource: AppSource = PackageManagerAppSource(context.packageManager)
    private val appLoader = AppLoader(appSource)
    private val favoritesManager = FavoritesManager(context)
    private val shortcutManager = ShortcutManager(context) // Add ShortcutManager

    // BroadcastReceiver to listen for package changes
    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Refresh the list when a package is added, removed, or changed
            refreshApps()
        }
    }

    init {
        // Register the receiver to listen for package install/uninstall events
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        getApplication<Application>().registerReceiver(packageChangeReceiver, filter)

        refreshApps()
    }

    fun refreshApps() {
        // Launch a coroutine in viewModelScope that runs on the IO dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            // Load all apps on a background thread
            val allApps = appLoader.loadAndSortApps().map { appInfo ->
                appInfo.apply { isFavorite = favoritesManager.isFavoriteApp(appInfo.packageName) }
            }

            // Prepare the display list on the background thread
            val displayList = prepareDisplayList(allApps)

            // Switch to the Main thread to post the value to LiveData
            withContext(Dispatchers.Main) {
                _items.value = displayList
            }
        }
    }

    private fun prepareDisplayList(allApps: List<AppInfo>): List<ListItem> {
        val displayList = mutableListOf<ListItem>()

        // Widget Area
        val displayMetrics = context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        displayList.add(ListItem.WidgetItem((screenHeight * 0.40).toInt()))

        // Partition shortcuts into favorite and non-favorite
        val allShortcuts = shortcutManager.getShortcuts()
        val favoriteShortcuts = allShortcuts.filter { favoritesManager.isFavoriteShortcut(it) }
        val nonFavoriteShortcuts = allShortcuts.filterNot { favoritesManager.isFavoriteShortcut(it) }

        // User's Favorites section
        val favoriteApps = allApps.filter { it.isFavorite }
        if (favoriteApps.isNotEmpty() || favoriteShortcuts.isNotEmpty()) {
            displayList.add(ListItem.HeaderItem(context.getString(R.string.header_favorites)))
            displayList.addAll(favoriteApps.map { ListItem.UserAppItem(it) })
            displayList.addAll(favoriteShortcuts.map { ListItem.ShortcutItem(it) })
        }

        // User Launchable Items (Apps + Shortcuts) section
        val nonFavoriteApps = allApps.filter { !it.isFavorite }

        // Create a combined list of ListItem objects
        val userLaunchableList = mutableListOf<ListItem>()

        // Add non-favorite apps
        userLaunchableList.addAll(nonFavoriteApps.filter { it.name.isNotEmpty() }.map { ListItem.UserAppItem(it) })

        // Add non-favorite shortcuts
        userLaunchableList.addAll(nonFavoriteShortcuts.map { ListItem.ShortcutItem(it) })

        // Group the combined list alphabetically
        userLaunchableList
            .groupBy { item ->
                val name = when (item) {
                    is ListItem.UserAppItem -> item.appInfo.name
                    is ListItem.ShortcutItem -> item.shortcutInfo.shortLabel?.toString() ?: ""
                    else -> "" // Should not be reached
                }
                val firstChar = name.firstOrNull()
                if (firstChar == null || firstChar.isDigit()) '#' else firstChar.uppercaseChar()
            }
            .toSortedMap()
            .forEach { (headerChar, items) ->
                displayList.add(ListItem.HeaderItem(headerChar.toString()))
                displayList.addAll(items)
            }

        // MinimalLauncher Apps section
        displayList.add(ListItem.HeaderItem(context.getString(R.string.header_launcher_apps)))
        displayList.add(
            ListItem.InternalActivityItem(
                title = context.getString(R.string.title_launcher_settings),
                // Note: This requires an Activity context, but ViewModel has Application context.
                // For launching, the context from the fragment is better. We'll pass it in the fragment.
                // However, for creating the Intent, Application context is fine.
                intent = android.content.Intent(context, SettingsActivity::class.java)
            )
        )

        return displayList
    }

    fun performSearch(query: String, allItems: List<ListItem>, appsHeader: String, launcherAppsHeader: String, emptyStateResId: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val finalList = mutableListOf<ListItem>()
            val lowerCaseQuery = query.lowercase(Locale.getDefault())

            if (lowerCaseQuery.isNotEmpty()) {
                // Filter all launchable items
                val allMatchingItems = allItems.filterIsInstance<ListItem.Launchable>().filter { item ->
                    when (item) {
                        is ListItem.UserAppItem -> item.appInfo.name.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
                        is ListItem.InternalActivityItem -> item.title.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
                        is ListItem.ShortcutItem -> item.shortcutInfo.shortLabel?.toString()?.lowercase(Locale.getDefault())?.contains(lowerCaseQuery) == true
                        else -> false
                    }
                }

                // Group items by category
                val apps = allMatchingItems.filterIsInstance<ListItem.UserAppItem>() + allMatchingItems.filterIsInstance<ListItem.ShortcutItem>()
                val internalItems = allMatchingItems.filterIsInstance<ListItem.InternalActivityItem>()

                // Add headers and their corresponding items
                if (apps.isNotEmpty()) {
                    finalList.add(ListItem.HeaderItem(appsHeader))
                    finalList.addAll(apps)
                }
                if (internalItems.isNotEmpty()) {
                    finalList.add(ListItem.HeaderItem(launcherAppsHeader))
                    finalList.addAll(internalItems)
                }
            }

            // If no results were found, add the empty state item
            if (finalList.isEmpty()) {
                finalList.add(ListItem.EmptyStateItem(emptyStateResId))
            }

            _searchResults.postValue(finalList)
        }
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * This is the perfect place to unregister the BroadcastReceiver.
     */
    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(packageChangeReceiver)
    }
}
