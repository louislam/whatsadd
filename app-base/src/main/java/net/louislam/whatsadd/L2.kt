package net.louislam.whatsadd

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import java.text.SimpleDateFormat
import java.util.*


class L2 {
    companion object {
        fun inputDialog2(c: Context, msg: String, okListener: (String) -> Unit) {
            inputDialog2(c, msg, okListener, null)
        }

        fun inputDialog2(c: Context, msg: String, okListener: (String) -> Unit, defaultValue: String?) {
            val alert = AlertDialog.Builder(c)

            alert.setTitle(msg)

            // Set an EditText view to get user input
            val input = EditText(c)

            if (defaultValue != null) {
                input.setText(defaultValue)
            }

            input.setSingleLine()
            val container = FrameLayout(c)
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.leftMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin)
            params.rightMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin)
            input.layoutParams = params
            container.addView(input);
            alert.setView(container)

            alert.setPositiveButton("OK",
                    object : DialogInterface.OnClickListener {
                        override fun onClick(
                                dialog: DialogInterface,
                                whichButton: Int) {
                            val value = input.getText()
                                    .toString()

                            if (value != "")        
                                okListener.invoke(value)
                        }
                    })

            alert.setNegativeButton("Cancel", null)
            alert.show()
        }
    }


}

fun Date.iso() : String {
    val sdf = SimpleDateFormat("yyyy-mm-dd HH:mm:ss")
    return sdf.format(this)
}