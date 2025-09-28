package me.samud.minimumlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppListAdapter(
    private var apps: List<AppInfo>, // Changed to var to allow updating with filtered list
    private val listener: OnAppClickListener
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>(), Filterable { // Implement Filterable

    private var appsFiltered: MutableList<AppInfo> = apps.toMutableList() // Keep a copy for filtering

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
        val app = appsFiltered[position] // Use filtered list
        holder.appName.text = app.name
        // Set click listener to launch the app
        holder.itemView.setOnClickListener {
            listener.onAppClick(app.packageName)
        }
    }

    override fun getItemCount(): Int {
        return appsFiltered.size // Use filtered list size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString()?.lowercase(Locale.getDefault()) ?: ""
                appsFiltered = if (charString.isEmpty()) {
                    apps.toMutableList()
                } else {
                    val filteredList = mutableListOf<AppInfo>()
                    for (app in apps) {
                        if (app.name.lowercase(Locale.getDefault()).contains(charString)) {
                            filteredList.add(app)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = appsFiltered
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                appsFiltered = results?.values as MutableList<AppInfo>? ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
