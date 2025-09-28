package me.samud.minimumlauncher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppListFragment : Fragment(), AppListAdapter.OnAppClickListener { // Implement OnAppClickListener

    private lateinit var recyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter
    private lateinit var appLoader: AppLoader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_list, container, false)
        recyclerView = view.findViewById(R.id.app_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Create AppSource implementation
        val appSource: AppSource = PackageManagerAppSource(requireActivity().packageManager)
        // Initialize AppLoader with the AppSource
        appLoader = AppLoader(appSource)

        loadApps()

        return view
    }

    private fun loadApps() {
        val apps = appLoader.loadAndSortApps()
        // Pass this fragment as the listener
        appListAdapter = AppListAdapter(apps, this)
        recyclerView.adapter = appListAdapter
    }

    // Implementation of OnAppClickListener
    override fun onAppClick(packageName: String) {
        val launchIntent = requireActivity().packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            startActivity(it)
        }
    }
}
