package monster.minions.binocularss.ui

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A class to translate a publishing date into a "units of time ago" format. For example,
 * "10 minutes ago"
 *
 * @param pubDate The publishing date of the object.
 * @param shortOutput Whether the function should return output as "5m" or "5 minutes ago".
 * @return A string describing the time since a publishing date.
 */
@SuppressLint("SimpleDateFormat")
fun getTime(pubDate: String, shortOutput: Boolean = false): String {
    // Set time as pubDate as a fall back.
    var time = pubDate
    // The date formats to try.
    val dateFormats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "EEE, dd MMM yyyy HH:mm zzz",
        "EEE, dd MMM yyyy HH:mm zzz"
    )
    var diff = 0L

    // Try all three date formats, exit when one works.
    for (dateFormat in dateFormats) {
        try {
            val articleDateFormat = SimpleDateFormat(dateFormat)
            val date = articleDateFormat.parse(pubDate)
            diff = Date().time - date!!.time
        } catch (e: ParseException) {
        }
    }

    // Convert the time in milliseconds into a string.
    when {
        // Minutes.
        diff < 1000L * 60L * 60L -> {
            val longEnding =
                if ((diff / (1000L * 60L)) == 1L) " minute ago" else " minutes ago"
            time = "${diff / (1000L * 60L)}" + if (shortOutput) "m" else longEnding
        }
        // Hours.
        diff < 1000L * 60L * 60L * 24L -> {
            val longEnding =
                if ((diff / (1000L * 60L * 60L)) == 1L) " hour ago" else " hours ago"
            time = "${diff / (1000L * 60L * 60L)}" + if (shortOutput) "h" else longEnding
        }
        // Days.
        diff < 1000L * 60L * 60L * 24L * 30L -> {
            val longEnding =
                if ((diff / (1000L * 60L * 60L * 24L)) == 1L) " day ago" else " days ago"
            time = "${diff / (1000L * 60L * 60L * 24L)}" + if (shortOutput) "d" else longEnding
        }
        // Months.
        diff < 1000L * 60L * 60L * 24L * 30L * 12L -> {
            val longEnding =
                if ((diff / (1000L * 60L * 60L * 24L * 30L)) == 1L) " month ago" else " months ago"
            time =
                "${diff / (1000L * 60L * 60L * 24L * 30L)}" + if (shortOutput) "M" else longEnding
        }
        // Years.
        else -> {
            val longEnding =
                if ((diff / (1000L * 60L * 60L * 24L * 30L * 12L)) == 1L) " year ago" else " years ago"
            time =
                "${diff / (1000L * 60L * 60L * 24L * 30L * 12L)}" + if (shortOutput) "Y" else longEnding
        }
    }

    return time
}