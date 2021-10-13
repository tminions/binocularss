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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
    Feed feed;

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

        mFetchFeedButton.setOnClickListener(view -> {
            // Get feed url from EditText
            String feedUrl = mEditText.getText().toString();
            Log.e("Edit Text", "Edit Text: " + mEditText.getText().toString());

            // If EditText is empty, set the url to cbc for testing purposes
            if (feedUrl.equalsIgnoreCase("")) {
                feedUrl = "https://rss.cbc.ca/lineup/topstories.xml";
            }
            feed = new Feed(feedUrl);
            new PullFeedTask().execute(feed);
        });

        mSwipeLayout.setOnRefreshListener(() -> {
            // Get feed url from EditText
            String feedUrl = mEditText.getText().toString();
            Log.e("Edit Text", "Edit Text: " + mEditText.getText().toString());

            // If EditText is empty, set the url to cbc for testing purposes
            if (feedUrl.equalsIgnoreCase("")) {
                feedUrl = "https://rss.cbc.ca/lineup/topstories.xml";
            }
            feed = new Feed(feedUrl);
            new PullFeedTask().execute(feed);
        });
    }

    public static class PullFeedTask extends AsyncTask<Feed, Void, List<Feed>> {

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

                // Initialize an inputStreamReader for the RSS feed.
                try {
                    link = new URL(feed.getUrl());
                    inputStream = link.openConnection().getInputStream();
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

//        @Override
//        protected void onPostExecute(List<Feed> feeds) {
//            super.onPostExecute(feeds);
//
//            for (Feed feed : feeds) {
//                mFeedTitleTextView.setText(feed.getTitle());
//                mFeedDescriptionTextView.setText(feed.getDescription());
//                mFeedUrlTextView.setText(feed.getUrl());
//            }
//        }

    }
}