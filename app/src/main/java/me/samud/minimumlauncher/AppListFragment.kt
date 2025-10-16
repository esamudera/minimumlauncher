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
import java.util.Locale

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

        // Search UI
        searchView = view.findViewById(R.id.search_view)

        // Search Results RecyclerView
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        searchResultsAdapter = AppListAdapter(emptyList(), this, this, sharedViewModel)
        searchResultsRecyclerView.adapter = searchResultsAdapter

        setupSearch(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the AppListViewModel without a factory
        appListViewModel = ViewModelProvider(this)[AppListViewModel::class.java]

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

        // Observe favorites changes and trigger a refresh in the AppListViewModel
        sharedViewModel.favoritesChanged.observe(viewLifecycleOwner) { changed ->
            if (changed) {
                appListViewModel.refreshApps()
                sharedViewModel.onFavoritesChangedHandled()
            }
        }
    }

    private fun setupSearch(view: View) {
        searchView.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.SHOWING) {
                // Trigger search with an empty query when the view is shown
                performSearch("")
            } else if (newState == SearchView.TransitionState.HIDDEN) {
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
                performSearch(query)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val fabSearch = view.findViewById<FloatingActionButton>(R.id.fab_search)
        fabSearch.setOnClickListener {
            searchView.show()
        }
    }

    private fun performSearch(query: String) {
        val finalList = mutableListOf<ListItem>()
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        if (lowerCaseQuery.isNotEmpty()) {
            // Filter all launchable items
            val allMatchingItems = currentDisplayList.filterIsInstance<ListItem.Launchable>().filter { item ->
                when (item) {
                    is ListItem.UserAppItem -> item.appInfo.name.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
                    is ListItem.InternalActivityItem -> item.title.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
                    is ListItem.WebShortcutItem -> item.title.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
                    else -> false
                }
            }

            // Group items by category
            val apps = allMatchingItems.filterIsInstance<ListItem.UserAppItem>() + allMatchingItems.filterIsInstance<ListItem.WebShortcutItem>()
            val internalItems = allMatchingItems.filterIsInstance<ListItem.InternalActivityItem>()

            // Add headers and their corresponding items
            if (apps.isNotEmpty()) {
                finalList.add(ListItem.HeaderItem(getString(R.string.header_apps)))
                finalList.addAll(apps)
            }
            if (internalItems.isNotEmpty()) {
                finalList.add(ListItem.HeaderItem(getString(R.string.header_launcher_apps)))
                finalList.addAll(internalItems)
            }
        }

        // If no results were found, add the empty state item
        if (finalList.isEmpty()) {
            finalList.add(ListItem.EmptyStateItem(R.string.msg_empty_search_results))
        }

        searchResultsAdapter.updateItems(finalList)
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
