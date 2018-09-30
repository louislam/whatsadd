package net.louislam.whatsadd

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AbsListView
import android.widget.RelativeLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.louislam.android.L
import java.io.UnsupportedEncodingException
import java.lang.Thread.sleep
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

        number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                historyAdapter.filter(s!!)
            }
        })

        historyRecycleView.layoutManager = LinearLayoutManager(this)
        historyRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, scrollState: Int) {
                L.log("Scroll Status: $scrollState")
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                L.log("Scroll (dx,dy): ($dx,$dy)")

                var fixedDY : Float;

                var max = convertDpToPixel(-140F, this@KotlinMainActivity);

                if (toolbar.translationY - dy < max) {
                    fixedDY = max;
                } else if (toolbar.translationY - dy > 0) {
                    fixedDY = 0F;
                } else {
                    fixedDY = toolbar.translationY - dy;
                }

                toolbar.translationY = fixedDY
                titleView.translationY = fixedDY
                cardView.translationY = fixedDY
              //  historyAdapter.spaceView!!.height = 1000

          //      val layoutParams = historyRecycleView.layoutParams as RelativeLayout.LayoutParams
       //         layoutParams.topMargin = fixedDY.toInt() + convertDpToPixel(180F, this@KotlinMainActivity).toInt();
          //      historyRecycleView.layoutParams = layoutParams
                L.log("Toolbar Scroll: ${toolbar.translationY }")
                L.log("Space Scroll: ${historyAdapter.spaceView!!.height}")
                val topMargin = (historyRecycleView.layoutParams as RelativeLayout.LayoutParams).topMargin;
                L.log("HistoryView Scroll: ${topMargin}")
            }
        })

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

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun test() {
        Thread {
            val date = Date()

            // Test Data
            for(i in 10000000 .. 10000520) {
                date.time = date.time - 3600 * 12
                historyAdapter.add(Phone("852", i.toString(), date), false)
            }

            runOnUiThread {
                historyAdapter.notifyDataSetChanged()
            }
        }.start()

    }

    override fun openWhatsApp(packageName: String) {
        val areaCodeString = areaCode.text.toString().trim { it <= ' ' }
        val numberString = number.text.toString().trim { it <= ' ' }
        openWhatsApp(packageName, areaCodeString, numberString)
    }

    fun openWhatsApp(packageName: String, areaCodeString : String, numberString : String) {
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
                historyAdapter.clearFilter();

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
