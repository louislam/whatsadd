package net.louislam.whatsadd

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.louislam.android.L
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class KotlinMainActivity : MainActivity() {

    lateinit var historyAdapter : HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        number.setOnEditorActionListener { _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                button.performClick()
            }
            true
        }

        historyRecycleView.layoutManager = LinearLayoutManager(this)

        historyAdapter = HistoryAdapter(this)
        historyRecycleView.adapter = historyAdapter

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder : RecyclerView.ViewHolder, direction : Int) {
                val adapter = historyRecycleView.adapter as HistoryAdapter
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(historyRecycleView)
    }

    override fun openWhatsApp(packageName: String) {
        val areaCodeString = areaCode.text.toString().trim { it <= ' ' }
        val numberString = number.text.toString().trim { it <= ' ' }
        val url = "https://api.whatsapp.com/send?phone=" + URLEncoder.encode(areaCodeString + numberString, "utf-8")
        Log.v("URL", url)

        try {
            if (areaCodeString == "" || numberString == "") {
                Toast.makeText(this@KotlinMainActivity, R.string.please_input,
                        Toast.LENGTH_SHORT).show()
                return
            }

            L.storeString(this@KotlinMainActivity, "areaCode", areaCodeString)
            number.setText("")

            val intent = Intent()
            intent.component = ComponentName(packageName, "com.whatsapp.TextAndDirectChatDeepLink")

            val uri = Uri.parse(url)

            intent.data = uri
            startActivity(intent)


        } catch (ex: Exception) {
            L.log(ex.toString())

            try {
                val browse: Intent  = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                L.storeString(this@KotlinMainActivity, "areaCode", areaCodeString)
                number.setText("")
                startActivity(browse)

            } catch (e: UnsupportedEncodingException) {
                L.alert(this@KotlinMainActivity, "")
            } catch (e: ActivityNotFoundException) {
                L.alert(this@KotlinMainActivity, getString(R.string.need_browser))
            } catch (e: Exception) {
                L.alert(this@KotlinMainActivity, "Unable to find WhatsApp, please install first.")
            }

        }

        historyAdapter.add(Phone(areaCodeString, numberString, Date()))
    }
}
