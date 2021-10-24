package monster.minions.binocularss

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class PullFeedTask(@SuppressLint("StaticFieldLeak") val context: Context) :
    AsyncTask<Feed, Void, List<Feed>>() {
    val reference = WeakReference(context)
    private var stream: InputStream? = null

    /**
     * Asynchronous execution class that runs XML parser code off of the main thread to not
     * interrupt UI
     *
     * @param params An unspecified number of feeds to be updated
     * @return A list of updated feeds
     */
    override fun doInBackground(vararg params: Feed?): List<Feed> {
        val feeds = mutableListOf<Feed>()

        for (feed in params) {
            // Connect to url
            val connect = URL(feed?.link).openConnection() as HttpURLConnection
            connect.readTimeout = 8000;
            connect.connectTimeout = 8000;
            connect.requestMethod = "GET"
            connect.connect()

            val responseCode: Int = connect.responseCode
            var pulledFeed = Feed()

            // If the connection is successful, continue processing
            if (responseCode == 200) {
                // Connect an input stream reader
                stream = connect.inputStream

                try {
                    // Send the input stream to be parsed
                    val parser = RssParser()
                    pulledFeed = parser.parse(stream!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            if (feed != null) {
                // Add a union of the pulled feed and the pre-existing feed to the set of feeds
                feeds.add(mergeFeeds(feed, pulledFeed))
            }
        }

        // DEBUG
        // for (feed in feeds) {
        //     for(article in feed.articles) {
        //         val title = article.title
        //         val desc = article.description
        //         Log.d("test", "test")
        //         Log.d("test", "article: title: $title, description: $desc")
        //     }
        // }

        return feeds
    }

    /**
     * Update feed information based on the pulledFeed with the following criteria:
     * 1. Any element in newFeed will be added to the new list
     * 2. Any element in oldFeed that is not in newFeed will be added to the list
     * 3. The user set properties like tags and priority of oldFeed will be transferred to newFeed
     *
     * @param oldFeed The feed passed in by the program to be updated
     * @param newFeed The feed pulled down from the RSS
     * @return The merge of oldFeed and newFeed as described above
     */
    private fun mergeFeeds(oldFeed: Feed, newFeed: Feed): Feed {
        val unionArticles = mutableListOf<Article>()

        // Satisfy property 2 from the docstring
        for (article in oldFeed.articles) {
            var anyEquals = false;

            for (pulledArticle in newFeed.articles) {
                if (article == pulledArticle) {
                    anyEquals = true
                }
            }

            if (!anyEquals) {
                unionArticles.add(article)
            }
        }

        // Satisfy property 1 from the docstring
        unionArticles.addAll(newFeed.articles)

        // Transfer the user-set flags
        newFeed.tags = oldFeed.tags
        newFeed.priority = oldFeed.priority
        newFeed.articles = unionArticles

        return newFeed
    }

    /**
     * Function that runs after doInBackground to update any information that needs to be updated
     * after the async portion of the code.
     *
     * @param result result of doInBackground
     */
    override fun onPostExecute(result: List<Feed>?) {
        // TODO update ui or send something back to tell the ui to update
    }
}