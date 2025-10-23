package me.samud.minimumlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    private var items: List<ListItem>,
    private val listener: OnItemClickListener,
    private val longClickListener: OnItemLongClickListener? = null,
    private val sharedViewModel: SharedViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Interface for click events
    interface OnItemClickListener {
        fun onItemClick(item: ListItem.Launchable)
    }

    // Interface for long click events
    interface OnItemLongClickListener {
        fun onItemLongClick(item: ListItem, sharedViewModel: SharedViewModel)
    }

    // View holder for app items
    class LaunchableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appName: TextView = itemView.findViewById(R.id.app_name)
    }

    // View holder for header items
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTitle: TextView = itemView.findViewById(R.id.header_title)
    }

    // View holder for widget area
    class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(height: Int) {
            itemView.layoutParams.height = height
            itemView.requestLayout()
        }
    }

    // View holder for empty state
    class EmptyStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.empty_state_message)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.UserAppItem -> VIEW_TYPE_LAUNCHABLE
            is ListItem.InternalActivityItem -> VIEW_TYPE_LAUNCHABLE
            is ListItem.ShortcutItem -> VIEW_TYPE_LAUNCHABLE
            is ListItem.HeaderItem -> VIEW_TYPE_HEADER
            is ListItem.WidgetItem -> VIEW_TYPE_WIDGET
            is ListItem.EmptyStateItem -> VIEW_TYPE_EMPTY_STATE
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
            VIEW_TYPE_WIDGET -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.widget_area_item, parent, false)
                WidgetViewHolder(view)
            }
            VIEW_TYPE_EMPTY_STATE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.empty_state_item, parent, false)
                EmptyStateViewHolder(view)
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
                appHolder.itemView.setOnLongClickListener {
                    longClickListener?.onItemLongClick(item, sharedViewModel)
                    true
                }
            }
            is ListItem.InternalActivityItem -> {
                val appHolder = holder as LaunchableViewHolder
                appHolder.appName.text = item.title
                appHolder.itemView.setOnClickListener {
                    listener.onItemClick(item)
                }
            }
            is ListItem.ShortcutItem -> {
                val appHolder = holder as LaunchableViewHolder
                appHolder.appName.text = "${item.shortcutInfo.shortLabel} ↗"
                appHolder.itemView.setOnClickListener {
                    listener.onItemClick(item)
                }
                appHolder.itemView.setOnLongClickListener {
                    longClickListener?.onItemLongClick(item, sharedViewModel)
                    true
                }
            }
            is ListItem.HeaderItem -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.headerTitle.text = item.title
            }
            is ListItem.WidgetItem -> {
                val widgetHolder = holder as WidgetViewHolder
                widgetHolder.bind(item.height)
            }
            is ListItem.EmptyStateItem -> {
                val emptyStateHolder = holder as EmptyStateViewHolder
                emptyStateHolder.message.setText(item.messageResId)
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

    companion object {
        private const val VIEW_TYPE_LAUNCHABLE = 0
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_WIDGET = 2
        private const val VIEW_TYPE_EMPTY_STATE = 3
    }
}
