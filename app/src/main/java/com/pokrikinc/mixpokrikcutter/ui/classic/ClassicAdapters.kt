package com.pokrikinc.mixpokrikcutter.ui.classic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.R

data class SimpleListItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val actionLabel: String = "Открыть",
    val imageUrl: String? = null
)

data class QueueListItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val actionLabel: String = "Открыть очередь"
)

data class OrderListItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val status: String
)

data class PartListItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val canPrint: Boolean,
    val imageUrl: String? = null
)

class SimpleListAdapter(
    private val onClick: (SimpleListItem) -> Unit
) : RecyclerView.Adapter<SimpleListAdapter.ViewHolder>() {
    private val items = mutableListOf<SimpleListItem>()

    fun submitList(newItems: List<SimpleListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_list, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        itemView: View,
        private val onClick: (SimpleListItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.item_image)
        private val badgeView: TextView = itemView.findViewById(R.id.item_badge)
        private val titleView: TextView = itemView.findViewById(R.id.item_title)
        private val subtitleView: TextView = itemView.findViewById(R.id.item_subtitle)
        private val actionButton: Button = itemView.findViewById(R.id.item_action)
        private var item: SimpleListItem? = null

        init {
            itemView.setOnClickListener { item?.let(onClick) }
            actionButton.setOnClickListener { item?.let(onClick) }
        }

        fun bind(item: SimpleListItem) {
            this.item = item
            badgeView.text = item.title.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            badgeView.visibility = View.VISIBLE
            ClassicImageLoader.load(imageView, item.imageUrl) { hasImage ->
                badgeView.visibility = if (hasImage) View.GONE else View.VISIBLE
            }
            titleView.text = item.title
            subtitleView.text = item.subtitle
            subtitleView.visibility = if (item.subtitle.isNullOrBlank()) View.GONE else View.VISIBLE
            actionButton.text = item.actionLabel
        }
    }
}

class QueueListAdapter(
    private val onClick: (QueueListItem) -> Unit
) : RecyclerView.Adapter<QueueListAdapter.ViewHolder>() {
    private val items = mutableListOf<QueueListItem>()

    fun submitList(newItems: List<QueueListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_queue, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        itemView: View,
        private val onClick: (QueueListItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val badgeView: TextView = itemView.findViewById(R.id.queue_badge)
        private val titleView: TextView = itemView.findViewById(R.id.queue_title)
        private val subtitleView: TextView = itemView.findViewById(R.id.queue_subtitle)
        private val actionButton: Button = itemView.findViewById(R.id.queue_action)
        private var item: QueueListItem? = null

        init {
            itemView.setOnClickListener { item?.let(onClick) }
            actionButton.setOnClickListener { item?.let(onClick) }
        }

        fun bind(item: QueueListItem) {
            this.item = item
            badgeView.text = item.id.toString()
            titleView.text = item.title
            subtitleView.text = item.subtitle
            actionButton.text = item.actionLabel
        }
    }
}

class OrderListAdapter : RecyclerView.Adapter<OrderListAdapter.ViewHolder>() {
    private val items = mutableListOf<OrderListItem>()

    fun submitList(newItems: List<OrderListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.order_title)
        private val subtitleView: TextView = itemView.findViewById(R.id.order_subtitle)
        private val statusView: TextView = itemView.findViewById(R.id.order_status)
        private val badgeView: TextView = itemView.findViewById(R.id.order_badge)

        fun bind(item: OrderListItem) {
            badgeView.text = item.id.toString()
            titleView.text = item.title
            subtitleView.text = item.subtitle
            statusView.text = item.status
            val statusBackground = if (item.status.contains("Не", ignoreCase = true)) {
                R.drawable.bg_status_warning
            } else {
                R.drawable.bg_status_ok
            }
            statusView.setBackgroundResource(statusBackground)
        }
    }
}

class PartListAdapter(
    private val onPrintClick: (PartListItem) -> Unit
) : RecyclerView.Adapter<PartListAdapter.ViewHolder>() {
    private val items = mutableListOf<PartListItem>()

    fun submitList(newItems: List<PartListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_part, parent, false)
        return ViewHolder(view, onPrintClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        itemView: View,
        private val onPrintClick: (PartListItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.part_image)
        private val titleView: TextView = itemView.findViewById(R.id.part_title)
        private val subtitleView: TextView = itemView.findViewById(R.id.part_subtitle)
        private val button: Button = itemView.findViewById(R.id.part_button)
        private var item: PartListItem? = null

        init {
            button.setOnClickListener { item?.let(onPrintClick) }
        }

        fun bind(item: PartListItem) {
            this.item = item
            ClassicImageLoader.load(imageView, item.imageUrl)
            titleView.text = item.title
            subtitleView.text = item.subtitle
            button.isEnabled = item.canPrint
            button.text = if (item.canPrint) {
                itemView.context.getString(R.string.action_print)
            } else {
                itemView.context.getString(R.string.print_unavailable)
            }
        }
    }
}
