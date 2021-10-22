package monster.minions.binocularss

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream

class RssParser {
    private var rssItems = mutableListOf<Article>()
    private var rssItem: Article = Article()
    private var text: String = ""

    /**
     * Parse the input stream into article objects
     *
     * @param inputStream input stream connected to the RSS Feed url.
     * @return mutable list of articles that have been read and parsed.
     */
    fun parse(inputStream: InputStream): Feed {
        var feed = Feed()

        try {
            // Create an xml pull parser factory and set variables
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)
            var eventType = parser.eventType
            var isItem = false
            var isChannel = false

            // Loop until end of file
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name

                // Case statement on xml parser output
                when (eventType) {
                    // If it's the start of an item tag, reset variables
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            isItem = true
                            rssItem = Article()
                        }
                        if (tagName.equals("channel", ignoreCase = true)) {
                            isChannel = true
                        }
                    }
                    // If it's text, save the text to a variable
                    XmlPullParser.TEXT -> {
                        Log.d("parser", text)
                        text = parser.text
                    }
                    // If the tag is ending, save the text to the appropriate variable in Article
                    XmlPullParser.END_TAG -> {

                        if (isItem) {
                            when (tagName.lowercase()) {
                                "item" -> {
                                    rssItem.let { rssItems.add(it) }
                                    isItem = false
                                }
                                "title" -> {
                                    rssItem.title = text
                                }
                                "link" -> {
                                    rssItem.link = text
                                }
                                "description" -> {
                                    rssItem.description = text
                                }
                                "author" -> {
                                    rssItem.author = text
                                }
                                "category" -> {
                                    rssItem.category = text
                                }
                                "comments" -> {
                                    rssItem.comments = text
                                }
                                "enclosure" -> {
                                    rssItem.enclosure = text
                                }
                                "guid" -> {
                                    rssItem.guid = text
                                }
                                "pubDate" -> {
                                    rssItem.pubDate = text
                                }
                                "source" -> {
                                    rssItem.source = text
                                }
                            }
                        }

                        if (isChannel) {
                            when (tagName.lowercase()) {
                                "channel" -> {
                                    continue;
                                }
                                "title" -> {
                                    feed.title = text
                                }
                                "link" -> {
                                    feed.link = text
                                }
                                "description" -> {
                                    feed.description = text
                                }
                                "language" -> {
                                    feed.language = text
                                }
                                "copyright" -> {
                                    feed.copyright = text
                                }
                                "managingEditor" -> {
                                    feed.managingEditor = text
                                }
                                "webMaster" -> {
                                    feed.webMaster = text
                                }
                                "pubDate" -> {
                                    feed.pubDate = text
                                }
                                "lastBuildDate" -> {
                                    feed.lastBuildDate = text
                                }
                                "category" -> {
                                    feed.category = text
                                }
                                "generator" -> {
                                    feed.generator = text
                                }
                                "docs" -> {
                                    feed.docs = text
                                }
                                "cloud" -> {
                                    feed.cloud = text
                                }
                                "ttl" -> {
                                    feed.ttl = text
                                }
                                "image" -> {
                                    feed.image = text
                                }
                                "textInput" -> {
                                    feed.textInput = text
                                }
                                "skipHours" -> {
                                    feed.skipHours = text
                                }
                                "skipDays" -> {
                                    feed.skipDays = text
                                }
                            }
                        }
                    }
                }
                // Go to next line
                eventType = parser.next()
            }
            feed.articles = rssItems
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return feed
    }
}