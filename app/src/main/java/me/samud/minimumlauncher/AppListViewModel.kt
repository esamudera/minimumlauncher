package me.samud.minimumlauncher

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    private val _items = MutableLiveData<List<ListItem>>()
    val items: LiveData<List<ListItem>> = _items

    // Dependencies are now created inside the ViewModel
    private val context: Context = getApplication<Application>().applicationContext
    private val appSource: AppSource = PackageManagerAppSource(context.packageManager)
    private val appLoader = AppLoader(appSource)
    private val favoritesManager = FavoritesManager(context)

    init {
        refreshApps()
    }

    fun refreshApps() {
        // Launch a coroutine in viewModelScope that runs on the IO dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            // Load all apps on a background thread
            val allApps = appLoader.loadAndSortApps().map { appInfo ->
                appInfo.apply { isFavorite = favoritesManager.isFavorite(appInfo.packageName) }
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
        displayList.add(ListItem.WidgetItem((screenHeight * 0.45).toInt()))

        // User's Favorites section
        val favoritesList = allApps.filter { it.isFavorite }
        if (favoritesList.isNotEmpty()) {
            displayList.add(ListItem.HeaderItem(context.getString(R.string.header_favorites)))
            displayList.addAll(favoritesList.map { ListItem.UserAppItem(it) })
        }

        // Installed Apps section
        val otherAppsList = allApps.filter { !it.isFavorite }
        otherAppsList
            .filter { it.name.isNotEmpty() }
            .groupBy {
                val firstChar = it.name.first()
                if (firstChar.isDigit()) '#' else firstChar.uppercaseChar()
            }
            .toSortedMap()
            .forEach { (headerChar, apps) ->
                displayList.add(ListItem.HeaderItem(headerChar.toString()))
                displayList.addAll(apps.map { ListItem.UserAppItem(it) })
            }

        // MinimalLauncher Apps section
        displayList.add(ListItem.HeaderItem(context.getString(R.string.header_minimal_launcher_apps)))
        displayList.add(
            ListItem.InternalActivityItem(
                title = context.getString(R.string.title_minimal_launcher_settings),
                // Note: This requires an Activity context, but ViewModel has Application context.
                // For launching, the context from the fragment is better. We'll pass it in the fragment.
                // However, for creating the Intent, Application context is fine.
                intent = android.content.Intent(context, SettingsActivity::class.java)
            )
        )

        return displayList
    }
}
