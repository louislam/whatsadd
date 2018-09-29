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


class HistoryAdapter(private val context: KotlinMainActivity) : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        const val SPACE = 0
        const val ITEM = 1
    }

    private var displayItems : ArrayList<Phone> = ArrayList()
    private val items : ArrayList<Phone>
    private val gson = Gson()

    init {
        val listType = object : TypeToken<ArrayList<Phone>>() {}.type
        val json = LStorage.getString(context, "history");

        setHasStableIds(true)

        if (json == null) {
            items = ArrayList()
            items.add(Phone("", "", Date()))
        } else {
            items = gson.fromJson(json, listType);
        }

        displayItems.addAll(items);
    }

    /**
     * Add a item
     */
    fun add(phone : Phone) {
        items.add(1, phone)
        displayItems.add(1, phone)

        this.notifyItemInserted(1);
        save();
    }

    override fun getItemId(position: Int): Long {
        return displayItems[position].hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return displayItems.size
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

        val view = LayoutInflater.from(context).inflate(layout, parent, false);
        val viewHolder = ViewHolder(view)

        return viewHolder;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.itemViewType == ITEM) {
            val phone : Phone = displayItems[position]

            holder.itemView.historyItem.text = phone.toString()
            holder.itemView.dateView.text = phone.getTimeAgo(context)
            holder.itemView.firstChar.text = phone.getFirstChar()

            holder.itemView.setOnClickListener {
                context.number.setText(phone.number)
                context.areaCode.setText(phone.areaCode)
                context.button.performClick()
            }
        }
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        displayItems.removeAt(position)
        notifyItemRemoved(position)
        save();
    }

    fun save() {
        L.storeString(context, "history", gson.toJson(items))
    }

    fun filter(keyword : CharSequence) {

        if (keyword != "") {
            displayItems = items.filter { phone ->
                phone.toString().contains(keyword) || phone.number == ""
            } as ArrayList<Phone>
        } else {
            displayItems = items;
        }

        notifyDataSetChanged()
    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view)