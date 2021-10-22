package monster.minions.binocularss

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
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

            // TODO: Don't remove old ones articles, if something has been updated (check link)
            //  then overwrite that and add new ones to the list
            // for (pulledArticle in pulledFeed.articles) {
            //     for (article in feed.articles) {
            //         if (pulledArticle.link == article.link) continue;
            //     }
            // }
            // for (pulledArticle in pulledFeed.articles) {
            //     if (feed.articles.contains(pulledArticle)) {
            //
            //     }
            // }

            if (feed != null) {
                feed.articles = pulledFeed.articles
                feeds.add(feed)
            }
        }
        return feeds
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