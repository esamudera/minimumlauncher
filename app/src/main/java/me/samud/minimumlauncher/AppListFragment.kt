package me.samud.minimumlauncher

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AppListFragment : Fragment(), AppListAdapter.OnAppClickListener { // Implement OnAppClickListener

    private lateinit var recyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter
    private lateinit var appLoader: AppLoader
    private lateinit var searchEditText: EditText // Added EditText for search
    private lateinit var fabSearch: FloatingActionButton // Added FAB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_list, container, false)
        recyclerView = view.findViewById(R.id.app_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.search_edit_text) // Initialize EditText
        fabSearch = view.findViewById(R.id.fab_search) // Initialize FAB

        // Create AppSource implementation
        val appSource: AppSource = PackageManagerAppSource(requireActivity().packageManager)
        // Initialize AppLoader with the AppSource
        appLoader = AppLoader(appSource)

        loadApps()
        setupSearch() // Call method to setup search
        setupFab() // Call method to setup FAB

        return view
    }

    private fun loadApps() {
        val apps = appLoader.loadAndSortApps()
        // Pass this fragment as the listener
        appListAdapter = AppListAdapter(apps, this)
        recyclerView.adapter = appListAdapter
    }

    // Method to setup search functionality
    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                appListAdapter.filter.filter(s) // Call filter on the adapter
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed for now
            }
        })
    }

    private fun setupFab() {
        fabSearch.setOnClickListener {
            searchEditText.requestFocus()
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    // Implementation of OnAppClickListener
    override fun onAppClick(packageName: String) {
        val launchIntent = requireActivity().packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            startActivity(it)
        }
    }
}
