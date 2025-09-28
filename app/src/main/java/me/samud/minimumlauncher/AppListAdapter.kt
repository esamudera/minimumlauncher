package me.samud.minimumlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(private val apps: List<AppInfo>) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

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
        // TODO: Add click listener to launch the app
    }

    override fun getItemCount(): Int {
        return apps.size
    }
}