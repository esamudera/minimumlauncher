package me.samud.minimumlauncher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.search.SearchView

class AppListFragment : Fragment(), AppListAdapter.OnItemClickListener, AppListAdapter.OnItemLongClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchResultsAdapter: AppListAdapter
    private lateinit var searchView: SearchView
    private var currentDisplayList: List<ListItem> = emptyList()
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var appListViewModel: AppListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get the shared ViewModel, scoped to the activity
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_app_list, container, false)

        // Main RecyclerView
        recyclerView = view.findViewById(R.id.app_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Apply 40% screen height as top padding
        val screenHeight = requireActivity().getScreenHeight()
        
        // Read padding percentages from resources
        val topPaddingPercent = getString(R.string.padding_top_percent).toFloat()
        val bottomPaddingPercent = getString(R.string.padding_bottom_percent).toFloat()
        
        val topPadding = (screenHeight * topPaddingPercent).toInt()
        val bottomPadding = (screenHeight * bottomPaddingPercent).toInt()
        recyclerView.setPadding(0, topPadding, 0, bottomPadding)


        // Search UI
        searchView = view.findViewById(R.id.search_view)

        // Search Results RecyclerView
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        searchResultsAdapter = AppListAdapter(emptyList(), this, this, sharedViewModel)
        searchResultsRecyclerView.adapter = searchResultsAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the AppListViewModel without a factory
        appListViewModel = ViewModelProvider(this)[AppListViewModel::class.java]

        // Add this block to handle the back press for the SearchView
        val onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                // When the back button is pressed and the callback is enabled,
                // hide the search view. This will trigger its correct animation.
                searchView.hide()
            }
        }
        // Add the callback to the fragment's back press dispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        // Observe the app list from the ViewModel
        appListViewModel.items.observe(viewLifecycleOwner) { displayList ->
            // Store the current list for searching
            currentDisplayList = displayList

            // Initialize the adapter with the structured list
            if (::appListAdapter.isInitialized) {
                appListAdapter.updateItems(displayList)
            } else {
                appListAdapter = AppListAdapter(displayList, this, this, sharedViewModel)
                recyclerView.adapter = appListAdapter
            }
        }

        // Observe search results from the ViewModel
        appListViewModel.searchResults.observe(viewLifecycleOwner) { searchList ->
            searchResultsAdapter.updateItems(searchList)
        }

        // Observe favorites changes and trigger a refresh in the AppListViewModel
        sharedViewModel.favoritesChanged.observe(viewLifecycleOwner) { changed ->
            if (changed) {
                appListViewModel.refreshApps()
                sharedViewModel.onFavoritesChangedHandled()
            }
        }

        // Now, update the setupSearch method to enable/disable the callback
        setupSearch(view, onBackPressedCallback) // Pass the callback to setupSearch
    }

    private fun setupSearch(view: View, onBackPressedCallback: OnBackPressedCallback) {
        searchView.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.SHOWING) {
                // Enable the custom back press handler when the search view is shown
                onBackPressedCallback.isEnabled = true
                
                appListViewModel.performSearch(
                    query = "",
                    allItems = currentDisplayList,
                    appsHeader = getString(R.string.header_apps),
                    launcherAppsHeader = getString(R.string.header_launcher_apps),
                    emptyStateResId = R.string.msg_empty_search_results
                )
            } else if (newState == SearchView.TransitionState.HIDDEN) {
                // Disable it when the search view is hidden
                onBackPressedCallback.isEnabled = false
                searchView.editText.text.clear()
            }
        }
        searchView
            .editText
            .setOnEditorActionListener { v, actionId, event ->
                searchView.hide()
                false
            }
        searchView.editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                appListViewModel.performSearch(
                    query = query,
                    allItems = currentDisplayList,
                    appsHeader = getString(R.string.header_apps),
                    launcherAppsHeader = getString(R.string.header_launcher_apps),
                    emptyStateResId = R.string.msg_empty_search_results
                )
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val fabSearch = view.findViewById<FloatingActionButton>(R.id.fab_search)
        fabSearch.setOnClickListener {
            searchView.show()
        }
    }

    override fun onItemClick(item: ListItem.Launchable) {
        item.onLaunch(requireContext(), activity?.supportFragmentManager)
        // Hide search view after launching an app from search results
        if (searchView.isShowing) {
            searchView.hide()
        }
    }

    override fun onItemLongClick(item: ListItem, sharedViewModel: SharedViewModel) {
        if (item is ListItem.LongClickable) {
            item.onLongClick(requireContext(), activity?.supportFragmentManager, sharedViewModel)
        }
    }

    fun notInBaseFragment() : Boolean {
        return searchView.isShowing
    }

    fun resetUI() {
        // Hide the soft keyboard if it's open
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        activity?.currentFocus?.let { view ->
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        if (searchView.isShowing) {
            searchView.hide()
        }
        recyclerView.scrollToPosition(0)
    }
}
