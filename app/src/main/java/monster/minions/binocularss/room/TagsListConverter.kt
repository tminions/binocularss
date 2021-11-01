package monster.minions.binocularss.room

import androidx.room.TypeConverter
import com.google.gson.Gson

/**
 * A object to convert between a list of tags and CSV for Room database storage requirements.
 */
object TagsListConverter {
    private val gson = Gson()

    /**
     * Convert a list of tags to a CSV.
     * TODO ensure that tags (user entered) cannot include a comma or other special characters.
     *
     * @param tags A list of tags.
     * @return A string that contains comma separated values.
     */
    @TypeConverter
    fun toString(tags: List<String>?): String? {
        if (tags == null) return null

        val stringList = mutableListOf<String>()

        for (tag in tags) {
            stringList.add(tag)
        }

        return stringList.joinToString(",")
        // Gson implementation -- currently crashes (in the other method)
        // return gson.toJson(tags)
    }

    /**
     * Decode a CSV to a list of tags.
     *
     * @param str A string that contains comma separated values.
     * @return A list of tags.
     */
    @TypeConverter
    fun toTagList(str: String?): MutableList<String>? {
        if (str == null) return null
        return str.split(",") as MutableList<String>
        // Gson implementation -- currently crashes
        // val listType = object: TypeToken<List<String>>() {}.type
        // val tags = gson.fromJson<List<String>>(str, listType)
        // return tags.toMutableList()
    }
}
