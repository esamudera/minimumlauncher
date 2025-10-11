package me.samud.minimumlauncher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var allApps: List<AppInfo>
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
            if (newState == SearchView.TransitionState.HIDDEN) {
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
                val query = s?.toString()?.lowercase(Locale.getDefault()) ?: ""
                val filteredItems = if (query.isEmpty()) {
                    emptyList<ListItem>()
                } else {
                    // The search filter remains the same, using the flat allApps list
                    allApps.filter {
                        it.name.lowercase(Locale.getDefault()).contains(query)
                    }.map { ListItem.UserAppItem(it) }
                }
                searchResultsAdapter.updateItems(filteredItems)
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

    fun resetUI() {
        if (searchView.isShowing) {
            searchView.hide()
        }
        recyclerView.scrollToPosition(0)
    }
}
