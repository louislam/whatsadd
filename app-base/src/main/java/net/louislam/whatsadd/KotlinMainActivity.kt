package net.louislam.whatsadd

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
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
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.setting_page.*
import net.louislam.android.L
import net.louislam.android.LStorage
import org.jetbrains.anko.*
import org.json.JSONObject
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class KotlinMainActivity : MainActivity() {
    lateinit var historyAdapter : HistoryAdapter
    var maxOffset : Float = 0F;
    var currentPage = "add"

    override fun onCreate(savedInstanceState: Bundle?) {
        val disableHistory = LStorage.getBoolean(this, "disableHistory")
        val enableDarkTheme = LStorage.getBoolean(this, "enableDarkTheme")
        enableDarkTheme(enableDarkTheme)

        super.onCreate(savedInstanceState)

        settingView.visibility = View.GONE

        maxOffset = convertDpToPixel(-140F, this@KotlinMainActivity);

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
                //L.log("Scroll Status: $scrollState")
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                L.log("Scroll (dx,dy): ($dx,$dy)")

                val fixedDY : Float;

                if (toolbar.translationY - dy < maxOffset) {
                    fixedDY = maxOffset;
                } else if (toolbar.translationY - dy > 0) {
                    fixedDY = 0F;
                } else {
                    fixedDY = toolbar.translationY - dy;
                }

                setHeaderOffset(fixedDY)
                //L.log("Toolbar Scroll: ${toolbar.translationY }")
                //val topMargin = (historyRecycleView.layoutParams as RelativeLayout.LayoutParams).topMargin;
                //L.log("HistoryView Scroll: ${topMargin}")
            }
        })

        historyRecycleView.addOnLayoutChangeListener{ view: View, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int ->
            if (historyAdapter.itemCount <= 5 && currentPage == "add") {
                resetPosition()
            }
        }

        historyAdapter = HistoryAdapter(this, disableHistory)
        historyRecycleView.adapter = historyAdapter

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder : RecyclerView.ViewHolder, direction : Int) {
                val adapter = historyRecycleView.adapter as HistoryAdapter
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(historyRecycleView)

        settingView.animate().setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                if (currentPage == "add") {
                    settingView.visibility = View.GONE
                }
            }
        });

        defaultWhatsappButton.setOnClickListener {
            showDefaultWhatsappDialog()
        }

        darkThemeSwitch.isChecked = enableDarkTheme
        darkThemeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            LStorage.store(this, "enableDarkTheme", isChecked)
            toast(getString(R.string.theme_changed));
            recreate()
        }

        clearAllHistoryButton.setOnClickListener { _ ->
            alert(getString(R.string.history_clear_confirm_msg), getString(R.string.clear_all_history)) {
                yesButton {
                    historyAdapter.clear()
                    toast(getString(R.string.cleared))
                }
                noButton { }
            }.show()
        }

        disableHistorySwitch.isChecked = disableHistory
        disableHistorySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                alert(getString(R.string.history_clear_confirm_msg), getString(R.string.disable_history)) {
                    yesButton {
                        historyAdapter.disable = true
                    }
                    noButton {
                        disableHistorySwitch.isChecked = false
                    }
                }.show().setCancelable(false)
            } else {
                historyAdapter.disable = false
            }
        }

        exportButton.setOnClickListener {
            val formats = listOf("CSV", "JSON")
            selector(getString(R.string.export_format), formats) { _, i ->
                val format = formats[i]

                try {

                    // Make Folder
                    val file = getExternalFilesDir(null)
                    val file2 = File(file, "export")
                    var filename = ""
                    file2.mkdirs()

                    val sdf = SimpleDateFormat("yyyyMMdd-hhmmss")
                    val date = sdf.format(Date())

                    if (format == "JSON") {
                        filename = "export-$date.json"
                        val file3 = File(file2, filename)

                        val rootObj = JSONObject(historyAdapter.getJSON()!!)

                        val gson = GsonBuilder().setPrettyPrinting().create();
                        val jp =  JsonParser();
                        val je = jp.parse(rootObj.getJSONObject("hashMap").toString());
                        file3.writeText(gson.toJson(je))


                    } else if (format == "CSV") {
                        filename = "export-$date.csv"
                        val file3 = File(file2, filename)
                        historyAdapter.saveCSV(file3)

                    }

                    longToast("Saved: ${filename}")
                } catch (ex : java.lang.Exception) {
                    alert("Cannot write file: ${ex.message}").show();
                }
            }
        }

    }


    fun enableDarkTheme(enable : Boolean) {
        Log.v("Theme", "Use Dark Theme: $enable")
        val theme = super.getTheme();

        if (enable) {
            theme.applyStyle(R.style.DarkAppTheme, true);
        } else {
            theme.applyStyle(R.style.AppTheme, true);
        }
    }

    fun setHeaderOffset(value : Float) {
        toolbar.translationY = value
        titleView.translationY = value
        cardView.translationY = value
    }

    fun setHeaderOffsetWithAnimation(value : Float) {
        val time = 200L
        ObjectAnimator.ofFloat(toolbar, "translationY", value).setDuration(time).start()
        ObjectAnimator.ofFloat(titleView, "translationY", value).setDuration(time).start()
        ObjectAnimator.ofFloat(cardView, "translationY", value).setDuration(time).start()
    }

    fun resetPosition() {
        setHeaderOffsetWithAnimation(0F)
    }

    override fun openSettingPage() {
        currentPage = "setting"
        MainActivity.hideKeyboard(this)
        setHeaderOffsetWithAnimation(maxOffset)
        settingView.visibility = View.VISIBLE

        settingView.animate()
                .alpha(1f)
                .setDuration(200)
    }

    override fun openAddPage() {
        currentPage = "add"
        setHeaderOffsetWithAnimation(0F)

        settingView.animate()
            .alpha(0.0f)
            .setDuration(200)
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
