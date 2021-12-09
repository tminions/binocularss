package monster.minions.binocularss.operations

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import monster.minions.binocularss.dataclasses.Feed
import java.io.File
import java.io.FileInputStream

/**
 * A class that reads a JSON file of feed URLs and converts them to strings.
 */
class ImportData {
    private val gson: Gson = Gson()

    /**
     * Imports the saved feeds URLs from the given JSON file.
     *
     * @param context The context for which the function is called in
     * @return the JSON of feed URLs in String format
     */
    fun importFile(context: Context): String {
        val path = context.getFilesDir()
        val letDirectory = File(path,"LET")
        letDirectory.mkdirs()
        val file = File(letDirectory, "binocularss-export.json")
        val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
        return inputAsString
    }

    /**
     * Converts a String representation of a JSON file of feed URLs to a list of feed URLs.
     *
     * @param str The string representation of the JSON of feed URLs.
     * @return A list of the saved feed URLs.
     */
    fun toUrlList(str: String?): MutableList<String> {
        if (str != "") {
            val listType = object : TypeToken<List<String>>() {}.type
            val feeds = gson.fromJson<List<String>>(str, listType)
            return feeds.toMutableList()
        } else {
            return mutableListOf()
        }
    }
}