package me.samud.minimumlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    private var apps: List<AppInfo>,
    private val listener: OnAppClickListener
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    // Interface for click events
    interface OnAppClickListener {
        fun onAppClick(packageName: String)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appName: TextView = itemView.findViewById(R.id.app_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.appName.text = app.name
        // Set click listener to launch the app
        holder.itemView.setOnClickListener {
            listener.onAppClick(app.packageName)
        }
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }
}
