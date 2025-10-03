package me.samud.minimumlauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.OnBackPressedCallback
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.search.SearchView
import androidx.lifecycle.ViewModelProvider
import java.util.Locale

class AppListFragment : Fragment(), AppListAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchResultsAdapter: AppListAdapter
    private lateinit var appLoader: AppLoader
    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView
    private lateinit var allApps: List<AppInfo>
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get the shared ViewModel, scoped to the activity
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_app_list, container, false)

        // Set AppBarLayout height to ~40% of screen height
        val appBarLayout = view.findViewById<AppBarLayout>(R.id.app_bar)
        val displayMetrics = requireContext().resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        appBarLayout.layoutParams.height = (screenHeight * 0.40).toInt()

        appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            // Consider the AppBarLayout collapsed as soon as it starts to move up from the expanded position.
            val isCollapsed = verticalOffset < 0
            sharedViewModel.isAppBarCollapsed = isCollapsed
        }

        // Restore the collapsed/expanded state from the ViewModel after layout
        // appBarLayout.post {
            appBarLayout.setExpanded(!sharedViewModel.isAppBarCollapsed, false) // false = no animation
        // }

        // Main RecyclerView
        recyclerView = view.findViewById(R.id.app_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Search UI
        toolbar = view.findViewById(R.id.toolbar)
        searchView = view.findViewById(R.id.search_view)

        // Search Results RecyclerView
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        searchResultsAdapter = AppListAdapter(emptyList(), this)
        searchResultsRecyclerView.adapter = searchResultsAdapter

        val appSource: AppSource = PackageManagerAppSource(requireActivity().packageManager)
        appLoader = AppLoader(appSource)

        loadApps()
        setupSearch()

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchView.isShowing) {
                    searchView.editText.text.clear()
                    searchView.hide()
                }
                // If searchView is not showing, do nothing to prevent app from closing.
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        return view
    }

    private fun loadApps() {
        // Load all apps for search functionality
        allApps = appLoader.loadAndSortApps()

        // Create the structured list for display
        val displayList = mutableListOf<ListItem>()

        // User's Favorites section
        displayList.add(ListItem.HeaderItem("User's Favorites"))
        // Favorites list is empty for now

        // Installed Apps section
        displayList.add(ListItem.HeaderItem("Installed Apps"))
        displayList.addAll(allApps.map { ListItem.UserAppItem(it) })

        // MinimalLauncher Apps section
        displayList.add(ListItem.HeaderItem("MinimalLauncher Apps"))
        displayList.add(
            ListItem.InternalFragmentItem(
                title = "Minimal Launcher Settings",
                destination = SettingsFragment::class
            )
        )

        // Initialize the adapter with the structured list
        appListAdapter = AppListAdapter(displayList, this)
        recyclerView.adapter = appListAdapter
    }

    private fun setupSearch() {
        searchView
            .editText
            .setOnEditorActionListener { v, actionId, event ->
                searchView.hide()
                false
            }
        searchView.editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.lowercase(Locale.getDefault()) ?: ""
                val filteredApps = if (query.isEmpty()) {
                    emptyList()
                } else {
                    // The search filter remains the same, using the flat allApps list
                    allApps.filter {
                        it.name.lowercase(Locale.getDefault()).contains(query)
                    }
                }
                searchResultsAdapter.updateApps(filteredApps)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        toolbar.inflateMenu(R.menu.main_toolbar_menu)
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_search) {
                searchView.show()
                true
            } else {
                false
            }
        }
    }

    override fun onItemClick(item: ListItem.Launchable) {
        item.onLaunch(requireContext(), activity?.supportFragmentManager)
        // Hide search view after launching an app from search results
        if (searchView.isShowing) {
            searchView.hide()
        }
    }
}
