package net.louislam.whatsadd

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.history_item.view.*
import net.louislam.android.LStorage
import java.util.*
import com.google.gson.reflect.TypeToken
import net.louislam.android.L
import kotlin.collections.ArrayList


class HistoryAdapter(private val context: Context) : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        const val SPACE = 0
        const val ITEM = 1
    }

    private val items : ArrayList<Phone>
    private val gson = Gson()

    init {
        val listType = object : TypeToken<ArrayList<Phone>>() {}.type
        val json = LStorage.getString(context, "history");

        if (json == null) {
            items = ArrayList()
            items.add(Phone("", "", Date()))
        } else {
            items = gson.fromJson(json, listType);
        }
    }

    /**
     * Add a item
     */
    fun add(phone : Phone) {
        items.add(1, phone)
        this.notifyItemInserted(1);
        save();
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
            holder.itemView.historyItem.text = items[position].toString()
            holder.itemView.dateView.text = items[position].getTimeAgo(context)
        }
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        save();
    }

    fun save() {
        L.storeString(context, "history", gson.toJson(items))
    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view)