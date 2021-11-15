package monster.minions.binocularss.ui

import android.annotation.SuppressLint
import androidx.annotation.Nullable
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
// TODO fix
fun getTime(pubDate: String): String {
    var time = pubDate
    val dateFormats = listOf("EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm zzz", "EEE, dd MMM yyyy HH:mm zzz")
    var diff = 0L

    for (dateFormat in dateFormats) {
        try {
            val articleDateFormat = SimpleDateFormat(dateFormat)
            val date = articleDateFormat.parse(pubDate)
            diff = Date().time - date!!.time
        } catch (e: ParseException) {}
    }

    when {
        diff < 1000L * 60L * 60L -> {
            time = "${diff / (1000L * 60L)}m"
        }
        diff < 1000L * 60L * 60L * 24L -> {
            time = "${diff / (1000L * 60L * 60L)}h"
        }
        diff < 1000L * 60L * 60L * 24L * 30L -> {
            time = "${diff / (1000L * 60L * 60L * 24L)}d"
        }
        diff < 1000L * 60L * 60L * 24L * 30L * 12L -> {
            time = "${diff / (1000L * 60L * 60L * 24L * 30L)}M"
        }
        else -> {
            time = "${diff / (1000L * 60L * 60L * 24L * 30L * 12L)}Y"
        }
    }

    return time
}