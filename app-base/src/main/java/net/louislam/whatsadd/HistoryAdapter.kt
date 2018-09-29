package net.louislam.whatsadd

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.history_item.view.*
import java.util.*

class HistoryAdapter(private val context: Context) : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        const val SPACE = 0
        const val ITEM = 1
    }

    private val items : ArrayList<Phone> = ArrayList()

    init {
        items.add(Phone("", "", Date()))
    }

    fun add(phone : Phone) {
        items.add(1, phone)
        this.notifyItemInserted(1);
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return SPACE
        } else {
            return ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layout = R.layout.history_item
        if (viewType == SPACE) {
            layout = R.layout.space
        }
        return ViewHolder(LayoutInflater.from(context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.itemViewType == ITEM) {
            holder.itemView.historyItem.text = items.get(position).toString()
            holder.itemView.dateView.text = items.get(position).getTimeAgo(context)
        }
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }


}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
}