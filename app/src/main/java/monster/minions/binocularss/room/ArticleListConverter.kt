package monster.minions.binocularss.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import monster.minions.binocularss.dataclasses.Article

/**
 * A object to convert between a list of articles and JSON for Room database storage requirements.
 */
object ArticleListConverter {
    private val gson: Gson = Gson()

    /**
     * Convert a list of articles to a JSON.
     *
     * @param articleList A list of articles.
     * @return A string formatted as a JSON representing a list of Article objects
     */
    @TypeConverter
    fun toString(articleList: List<Article>?): String? {
        return gson.toJson(articleList)
    }

    /**
     * Convert a JSON string to a list of articles.
     *
     * @param str A string formatted as a JSON representing a list of Article objects
     * @return A list of articles.
     */
    @TypeConverter
    fun toArticleList(str: String?): MutableList<Article> {
        val listType = object: TypeToken<List<Article>>() {}.type
        val articles = gson.fromJson<List<Article>>(str, listType)
        return articles.toMutableList()
    }
}
