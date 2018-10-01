package net.louislam.whatsadd

import android.content.Context
import java.util.*
import com.choota.dev.ctimeago.TimeAgo

class Phone(val areaCode : String, val number : String, var date : Date) {
    var alias : String? = null

    override fun toString(): String {
        if (alias != null) {
            return alias!!
        } else {
            return getFullPhone()
        }
    }

    fun getFullPhone() : String {
        return "($areaCode) $number"
    }

    fun getTimeAgo(c : Context) : String {
        val timeAgo = TimeAgo().locale(c)
        return timeAgo.getTimeAgo(date)
    }

    fun getFirstChar() : String {
        if (alias != null && alias!!.length != 0) {
            return alias!!.first().toUpperCase().toString()
        } else if (number.length != 0) {
            return number.first().toString()
        } else {
            return "W"
        }
    }
}