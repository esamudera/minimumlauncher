package me.samud.minimumlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_list, container, false)
        recyclerView = view.findViewById(R.id.app_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadApps()

        return view
    }

    private fun loadApps() {
        val packageManager: PackageManager = requireActivity().packageManager
        val apps = mutableListOf<AppInfo>()

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val allApps: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in allApps) {
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val packageName = resolveInfo.activityInfo.packageName
            // val appIcon = resolveInfo.loadIcon(packageManager) // If you want to load icons
            apps.add(AppInfo(appName, packageName /*, appIcon */))
        }

        // Sort apps alphabetically
        apps.sortBy { it.name.lowercase() }

        appListAdapter = AppListAdapter(apps)
        recyclerView.adapter = appListAdapter
    }
}
