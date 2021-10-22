package monster.minions.binocularss

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream

class RssParser {
    private var rssItems = mutableListOf<Article>()
    private var rssItem : Article = Article()
    private var text: String = ""

    /**
     * Parse the input stream into article objects
     *
     * @param inputStream input stream connected to the RSS Feed url.
     * @return mutable list of articles that have been read and parsed.
     */
    fun parse(inputStream: InputStream): MutableList<Article> {
        try {
            // Create an xml pull parser factory and set variables
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)
            var eventType = parser.eventType
            var isItem = false

            // Loop until end of file
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name

                // Case statement on xml parser output
                when (eventType) {
                    // If it's the start of an item tag, reset variables
                    XmlPullParser.START_TAG ->
                        if (tagName.equals("item", ignoreCase = true)) {
                            isItem = true
                            rssItem = Article()
                        }
                    // If it's text, save the text to a variable
                    XmlPullParser.TEXT ->
                        text = parser.text
                    // If the tag is ending, save the text to the appropriate variable in Article
                    XmlPullParser.END_TAG ->
                        if (tagName.equals("item", ignoreCase = true)) {
                            rssItem.let { rssItems.add(it) }
                            isItem = false
                        } else if (isItem && tagName.equals("title", ignoreCase = true)) {
                            rssItem.title = text
                        // } else if (isItem && tagName.equals("link", ignoreCase = true)) {
                        //     rssItem.url = URL(text)
                        } else if (isItem && tagName.equals("pubDate", ignoreCase = true)) {
                            rssItem.date = text
                        // } else if (isItem && tagName.equals("category", ignoreCase = true)) {
                        //     rssItem= text.toString()
                        } else if (isItem && tagName.equals("description", ignoreCase = true)) {
                            rssItem.description = text
                        }
                }
                Log.d("parser", text)

                // Go to next line
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return rssItems
    }
}
