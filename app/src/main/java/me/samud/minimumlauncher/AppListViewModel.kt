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
    private val application: Application = getApplication()
    private val appSource: AppSource = PackageManagerAppSource(application.packageManager)
    private val appLoader = AppLoader(appSource)
    private val favoritesManager = FavoritesManager(application)
    private val shortcutManager = ShortcutManager(application) // Add ShortcutManager

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

        // Partition shortcuts into favorite and non-favorite
        val allShortcuts = shortcutManager.getShortcuts()
        val favoriteShortcuts = allShortcuts.filter { favoritesManager.isFavoriteShortcut(it) }

        // User's Favorites section
        val favoriteApps = allApps.filter { it.isFavorite }
        if (favoriteApps.isNotEmpty() || favoriteShortcuts.isNotEmpty()) {
            displayList.add(ListItem.HeaderItem(application.getString(R.string.header_favorites)))
            displayList.addAll(favoriteApps.map { ListItem.UserAppItem(it) })
            displayList.addAll(favoriteShortcuts.map { ListItem.ShortcutItem(it) })
        }

        // User Launchable Items (Apps + Shortcuts) section
        // We now include ALL apps and ALL shortcuts, not just non-favorites.
        val userLaunchableList = mutableListOf<ListItem>()

        // Add all apps
        userLaunchableList.addAll(allApps.filter { it.name.isNotEmpty() }.map { ListItem.UserAppItem(it) })

        // Add all shortcuts
        userLaunchableList.addAll(allShortcuts.map { ListItem.ShortcutItem(it) })

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
        displayList.add(ListItem.HeaderItem(application.getString(R.string.header_launcher_apps)))
        displayList.add(
            ListItem.InternalActivityItem(
                title = application.getString(R.string.title_launcher_settings),
                // Note: This requires an Activity context, but ViewModel has Application context.
                // For launching, the context from the fragment is better. We'll pass it in the fragment.
                // However, for creating the Intent, Application context is fine.
                intent = Intent(application, SettingsActivity::class.java)
            )
        )

        return displayList
    }

    fun performSearch(query: String, allItems: List<ListItem>, appsHeader: String, launcherAppsHeader: String, emptyStateResId: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val finalList = mutableListOf<ListItem>()
            val lowerCaseQuery = query.lowercase(Locale.getDefault())

            if (lowerCaseQuery.isNotEmpty()) {
                // Create sets to hold unique matching items
                val matchingApps = mutableSetOf<ListItem>()
                val matchingInternalItems = mutableSetOf<ListItem>()

                // Single pass to filter and categorize items
                for (item in allItems) {
                    // Use the new matches method from the ListItem itself
                    if (item.matches(lowerCaseQuery)) {
                        when (item) {
                            is ListItem.UserAppItem, is ListItem.ShortcutItem -> matchingApps.add(item)
                            is ListItem.InternalActivityItem -> matchingInternalItems.add(item)
                            else -> {}
                        }
                    }
                }

                // Build the final list from the unique sets
                if (matchingApps.isNotEmpty()) {
                    finalList.add(ListItem.HeaderItem(appsHeader))
                    finalList.addAll(matchingApps)
                }
                if (matchingInternalItems.isNotEmpty()) {
                    finalList.add(ListItem.HeaderItem(launcherAppsHeader))
                    finalList.addAll(matchingInternalItems)
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
