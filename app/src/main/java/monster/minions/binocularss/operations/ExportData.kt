package monster.minions.binocularss.operations

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContextCompat
import androidx.room.TypeConverter
import com.google.gson.Gson
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.room.ArticleListConverter
import java.io.File
import java.util.Date
import java.io.PrintWriter

/**
 * Exports the list of subscribed feeds as a JSON file to internal storage.
 */
class ExportData {
    private val gson: Gson = Gson()

    /**
     * Converts a list of feeds to a JSON containing the feed URL.
     *
     * @param feeds The list of feeds to be converted.
     * @return The string representation of the JSON file of feeds.
     */
    fun toJson(feeds: MutableList<Feed>): String? {
        val urls = mutableListOf<String>()
        for (feed in feeds) {
            urls.add(feed.source)
        }
        return gson.toJson(urls)
    }

    /**
     * Saves the given data as a JSON file to internal storage.
     *
     * @param context The context for which the function is called in.
     * @param data The data to be saved
     * @return Returns whether the save was successful or not.
     */
    fun exportFile(context: Context, data: String?): Boolean {
        val path = context.getFilesDir()
        val letDirectory = File(path, "LET")
        letDirectory.mkdirs()
        val file = File(letDirectory, "binocularss-export.json")

        // Used to clear the file of previously saved feeds
        val writer = PrintWriter(file)
        writer.close()

        return if (data != null) {
            file.appendText(data)
            true
        } else {
            false
        }
    }
}