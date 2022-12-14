package net.louislam.whatsadd

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.history_item.view.*
import net.louislam.android.LStorage
import java.util.*
import com.google.gson.reflect.TypeToken
import net.louislam.android.L
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import java.io.File
import java.nio.charset.Charset
import com.opencsv.CSVWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter


class HistoryAdapter(private val context: KotlinMainActivity, disable : Boolean) : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        const val SPACE = 0
        const val ITEM = 1
        const val STORE_NAME = "historyMap5"
    }

    var disable = disable
        set(value) {
            field = value
            LStorage.store(context, "disableHistory", value)
            if (value) {
                clear()
                notifyDataSetChanged()
            }
        }

    var items : MapArrayList<String, Phone>
    val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();


    init {
        val listType = object : TypeToken<MapArrayList<String, Phone>>() {}.type
        val json = getJSON()

        setHasStableIds(true)

        if (json == null) {
            items = initHistoryItems()
        } else {
            try {
                items = gson.fromJson(json, listType);
            } catch (ex : JsonSyntaxException) {
                items = initHistoryItems()
            }
        }
    }

    fun getJSON(): String? {
        return LStorage.getString(context, STORE_NAME);
    }

    private fun initHistoryItems() : MapArrayList<String, Phone> {
        val items : MapArrayList<String, Phone> = MapArrayList()
        val emptyPhone = Phone("", "", Date())
        items.add(0, emptyPhone.getFullPhone(), emptyPhone)
        return items
    }

    fun clear() {
        items = initHistoryItems()
        save()
        notifyDataSetChanged();
    }

    /**
     * Add a item
     */
    fun add(phone : Phone) {
        add(phone, true)
    }

    fun add(phone : Phone, notify : Boolean) {
        if (disable) {
            return;
        }

        val targetPhone : Phone;
        val foundPhone : Phone? = items.remove(phone.getFullPhone())

        // if found, bring to the top
        if (foundPhone != null) {
            targetPhone = foundPhone
            targetPhone.date = phone.date
        } else {
            targetPhone = phone
        }

        items.add(1, targetPhone.getFullPhone(), targetPhone)

        if (notify) {
            this.notifyItemInserted(1);
        }

        save();
    }



    override fun getItemId(position: Int): Long {
        return items.get(position)!!.hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return items.getSize()
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
            val phone : Phone = items.get(position)!!

            holder.itemView.historyItem.text = phone.toString()
            holder.itemView.dateView.text = phone.getTimeAgo(context)
            holder.itemView.firstChar.text = phone.getFirstChar()

            holder.itemView.setOnClickListener {
                context.number.setText(phone.number)
                context.areaCode.setText(phone.areaCode)
                context.button.performClick()
            }

            holder.itemView.setOnLongClickListener {
                L2.inputDialog2(context, context.getString(R.string.input_alias) + phone.getFullPhone() + ":", { alias ->
                    phone.alias = alias
                    notifyItemChanged(position)
                    save()
                }, phone.alias)

                true
            }
        }
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        save();
    }

    fun save() {
        L.storeString(context, STORE_NAME, gson.toJson(items))
    }

    fun filter(keyword : CharSequence) {
        if (keyword != "") {
            items.filter { key, phone ->
                key.contains(keyword) || phone.number == ""
            }
        } else {
            items.clearFilter();
        }

        notifyDataSetChanged()
    }

    fun clearFilter() {
        items.clearFilter()
    }

    fun saveCSV(file: File) {
        val header = arrayOf("Area Code", "Number", "Date", "Alias")

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charset.forName("UTF-8")).use { osw ->
                CSVWriter(osw).use { writer ->
                    writer.writeNext(header)

                    for((_, item) in items) {
                        val row = arrayOf( item.areaCode, item.number, item.date.iso(), item.alias)
                        writer.writeNext(row)
                    }
                }
            }
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view)