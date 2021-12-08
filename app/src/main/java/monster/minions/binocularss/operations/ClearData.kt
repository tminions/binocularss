package monster.minions.binocularss.operations

import android.content.Context
import java.io.File
import java.io.PrintWriter

/**
 * A class that clears the JSON file used for saving feed URLs.
 */
class ClearData {
    /**
     * A function that clears the JSON file of URLs.
     *
     * @param context The context for which the function is called in.
     */
    fun clearData(context: Context) {
        val path = context.getFilesDir()
        val letDirectory = File(path, "LET")
        letDirectory.mkdirs()
        val file = File(letDirectory, "binocularss-export.json")
        val writer = PrintWriter(file)
        writer.close()
    }
}