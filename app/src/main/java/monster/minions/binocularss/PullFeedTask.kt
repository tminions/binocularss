package monster.minions.binocularss

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection

class PullFeedTask(@SuppressLint("StaticFieldLeak") val context: Context) : AsyncTask<Feed, Void, List<Feed>>() {
    val reference = WeakReference(context)
    private var stream: InputStream ?= null

    /**
     * Asynchronous execution class that runs XML parser code off of the main thread to not
     * interrupt UI
     *
     * @param params An unspecified number of feeds to be updated
     * @return A list of updated feeds
     */
    override fun doInBackground(vararg params: Feed?): List<Feed> {
        var feeds = mutableListOf<Feed>()

        for (feed in params) {
            // Connect to url
            val connect = feed?.url?.openConnection() as HttpURLConnection
            connect.readTimeout = 8000;
            connect.connectTimeout = 8000;
            connect.requestMethod = "GET"
            connect.connect()

            val responseCode: Int = connect.responseCode
            var rssItems: List<Article> = mutableListOf<Article>()

            // If the connection is successful, continue processing
            if (responseCode == 200) {
                // Connect an input stream reader
                stream = connect.inputStream

                try {
                    // Send the input stream to be parsed
                    val parser = RssParser()
                    rssItems = parser.parse(stream!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            // TODO: replace this assignment with something that checks for
            //  duplacates and doesn't replace them to perserve bookmarks,
            //  read flags, and other things
            //-----------------------------+
            var articles = rssItems   //   |
            //-----------------------------+

            feed.articles = articles

            feeds.add(feed)
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