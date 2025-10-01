package me.samud.minimumlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    private var items: List<ListItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Interface for click events
    interface OnItemClickListener {
        fun onItemClick(item: ListItem.Launchable)
    }

    // View holder for app items
    class LaunchableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appName: TextView = itemView.findViewById(R.id.app_name)
    }

    // View holder for header items
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTitle: TextView = itemView.findViewById(R.id.header_title)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.UserAppItem -> VIEW_TYPE_LAUNCHABLE
            is ListItem.InternalFragmentItem -> VIEW_TYPE_LAUNCHABLE
            is ListItem.WebShortcutItem -> VIEW_TYPE_LAUNCHABLE
            is ListItem.HeaderItem -> VIEW_TYPE_HEADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LAUNCHABLE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.app_list_item, parent, false)
                LaunchableViewHolder(view)
            }
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.UserAppItem -> {
                val appHolder = holder as LaunchableViewHolder
                appHolder.appName.text = item.appInfo.name
                appHolder.itemView.setOnClickListener {
                    listener.onItemClick(item)
                }
            }
            is ListItem.InternalFragmentItem -> {
                val appHolder = holder as LaunchableViewHolder
                appHolder.appName.text = item.title
                appHolder.itemView.setOnClickListener {
                    listener.onItemClick(item)
                }
            }
            is ListItem.WebShortcutItem -> {
                val appHolder = holder as LaunchableViewHolder
                appHolder.appName.text = item.title
                appHolder.itemView.setOnClickListener {
                    listener.onItemClick(item)
                }
            }
            is ListItem.HeaderItem -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.headerTitle.text = item.title
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(newItems: List<ListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun updateApps(newApps: List<AppInfo>) {
        items = newApps.map { ListItem.UserAppItem(it) }
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_LAUNCHABLE = 0
        private const val VIEW_TYPE_HEADER = 1
    }
}
