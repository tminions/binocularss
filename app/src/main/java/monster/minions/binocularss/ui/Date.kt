package monster.minions.binocularss.ui

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
// TODO fix
fun getTime(pubDate: String, shortOutput: Boolean = false): String {
    var time = pubDate
    val dateFormats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "EEE, dd MMM yyyy HH:mm zzz",
        "EEE, dd MMM yyyy HH:mm zzz"
    )
    var diff = 0L

    for (dateFormat in dateFormats) {
        try {
            val articleDateFormat = SimpleDateFormat(dateFormat)
            val date = articleDateFormat.parse(pubDate)
            diff = Date().time - date!!.time
        } catch (e: ParseException) {

        }
    }

    when {
        diff < 1000L * 60L * 60L -> {
            time = "${diff / (1000L * 60L)}" + if (shortOutput) "m" else " months ago"
        }
        diff < 1000L * 60L * 60L * 24L -> {
            time = "${diff / (1000L * 60L * 60L)}" + if (shortOutput) "h" else " hours ago"
        }
        diff < 1000L * 60L * 60L * 24L * 30L -> {
            val longEnding = if ((diff / (1000L * 60L * 60L * 24L)) == 1L) " day ago" else " days ago"
            time = "${diff / (1000L * 60L * 60L * 24L)}" + if (shortOutput) "d" else longEnding
        }
        diff < 1000L * 60L * 60L * 24L * 30L * 12L -> {
            time =
                "${diff / (1000L * 60L * 60L * 24L * 30L)}" + if (shortOutput) "M" else " months ago"
        }
        else -> {
            time =
                "${diff / (1000L * 60L * 60L * 24L * 30L * 12L)}" + if (shortOutput) "Y" else " years ago"
        }
    }

    return time
}