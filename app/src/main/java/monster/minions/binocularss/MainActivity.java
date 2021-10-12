package monster.minions.binocularss;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    EditText mEditText;
    Button mFetchFeedButton;
    SwipeRefreshLayout mSwipeLayout;
    TextView mFeedTitleTextView;
    TextView mFeedUrlTextView;
    TextView mFeedDescriptionTextView;

    List<Article> mArticleList;
    String mFeedTitle;
    String mFeedUrl;
    String mFeedDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recycler_view);
        mEditText = findViewById(R.id.rss_feed_edit_text);
        mFetchFeedButton = findViewById(R.id.fetch_feed_button);
        mSwipeLayout = findViewById(R.id.swipe_refresh_layout);
        mFeedTitleTextView = findViewById(R.id.feed_title);
        mFeedDescriptionTextView = findViewById(R.id.feed_description);
        mFeedUrlTextView = findViewById(R.id.feed_url);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Feed feed = new Feed("https://rss.cbc.ca/lineup/topstories.xml");

        mFetchFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PullFeedTask().execute(feed);
            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new PullFeedTask().execute(feed);
            }
        });
    }

    public class PullFeedTask extends AsyncTask<Feed, Void, List<Feed>> {

        @Override
        protected List<Feed> doInBackground(Feed... feeds) {
            URL link;
            String itemTitle = "";
            String itemUrl = "";
            String itemDescription = "";
            boolean isItem = false;
            InputStream inputStream = null;
            List<Feed> returnFeeds = new ArrayList<Feed>();

            for (Feed feed : feeds) {

                List<Article> items = new ArrayList<Article>();

                // If the url does not already contain an http(s) at the beginning, add one.
                if (!feed.getUrl().startsWith("http://") && !feed.getUrl().startsWith("https://")) {
                    feed.setUrl("http://" + feed.getUrl());
                }

                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                        "<rss version=\"2.0\">\n" +
                        "<channel>\n" +
                        " <title>RSS Title</title>\n" +
                        " <description>This is an example of an RSS feed</description>\n" +
                        " <link>http://www.example.com/main.html</link>\n" +
                        " <copyright>2020 Example.com All rights reserved</copyright>\n" +
                        " <lastBuildDate>Mon, 06 Sep 2010 00:01:00 +0000</lastBuildDate>\n" +
                        " <pubDate>Sun, 06 Sep 2009 16:20:00 +0000</pubDate>\n" +
                        " <ttl>1800</ttl>\n" +
                        "\n" +
                        " <item>\n" +
                        "  <title>Example entry</title>\n" +
                        "  <description>Here is some text containing an interesting description.</description>\n" +
                        "  <link>http://www.example.com/blog/post/1</link>\n" +
                        "  <guid isPermaLink=\"false\">7bd204c6-1655-4c27-aeee-53f933c5395f</guid>\n" +
                        "  <pubDate>Sun, 06 Sep 2009 16:20:00 +0000</pubDate>\n" +
                        " </item>\n" +
                        "\n" +
                        "</channel>\n" +
                        "</rss>";

                // Initialize an inputStreamReader for the RSS feed.
                try {
                    link = new URL(feed.getUrl());
                    inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
                    // inputStream = link.openConnection().getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    // Create an XML parser.
                    XmlPullParser xmlPullParser = Xml.newPullParser();
                    xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    xmlPullParser.setInput(inputStream, null);

                    // Skip the first line of the XML.
                    xmlPullParser.nextTag();

                    // Loop through the XML and add the appropriate lines to an Article object.
                    while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                        int eventType = xmlPullParser.getEventType();

                        // Get the name of the current section of XML.
                        // String name = xmlPullParser.getName();
                        String name = xmlPullParser.getText();

                        if (name == null) {
                            continue;
                        }

                        // If it is a closing tag, go to the next section.
                        if (eventType == XmlPullParser.END_TAG) {
                            if (name.equalsIgnoreCase("item")) {
                                isItem = true;
                                continue;
                            }
                        }

                        // Log.d("XmlParser", "Parsing name ==> " + name);
                        Log.d("XmlParser", name);

                        // Get the text from one section of the XML and put it into a string.
                        String result = "";
                        if (xmlPullParser.next() == XmlPullParser.TEXT) {
                            result = xmlPullParser.getText();
                            xmlPullParser.nextTag();
                        }

                        // Assign lines to placeholder variables that will pass the information on to either
                        //// an Article or the Feed.
                        if (name.equalsIgnoreCase("title")) {
                            itemTitle = result;
                        } else if (name.equalsIgnoreCase("link")) {
                            itemUrl = result;
                        } else if (name.equalsIgnoreCase("description")) {
                            itemDescription = result;
                        }

                        if (itemTitle != null && itemUrl != null && itemDescription != null) {
                            if (isItem) {
                                // Add the appropriate information to an Article object and then
                                Article item = new Article(itemTitle, itemUrl, itemDescription);
                                items.add(item);
                            } else {
                                // If it is not an item, the information is for the Feed itself.
                                feed.setTitle(itemTitle);
                                feed.setUrl(itemUrl);
                                feed.setDescription(itemDescription);

                                Log.d("Test", itemTitle + " plus this is runnnig");
                                Log.d("Test", feed.getDescription());
                                Log.d("Test", feed.getUrl());
                            }

                            // Reset the variables for the next loop
                            itemTitle = null;
                            itemUrl = null;
                            itemDescription = null;
                            isItem = false;
                        }
                    }
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }

                feed.setArticles(items);

                for (Article article : feed.getArticles()) {
                    Log.d("Post Execute Article", "==>" + article.toString());
                }

                returnFeeds.add(feed);
            }

            return returnFeeds;
        }

        @Override
        protected void onPostExecute(List<Feed> feeds) {
            super.onPostExecute(feeds);

            for (Feed feed : feeds) {
//                Log.d("Post Execute", "This is running");
                Log.d("Post Execute", feed.getTitle() + " plus this is runnnig");
                Log.d("Post Execute", feed.getDescription());
                Log.d("Post Execute", feed.getUrl());


                mFeedTitleTextView.setText(feed.getTitle());
                mFeedDescriptionTextView.setText(feed.getDescription());
                mFeedUrlTextView.setText(feed.getUrl());
            }
        }
    }
}