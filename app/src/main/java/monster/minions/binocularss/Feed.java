package monster.minions.binocularss;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a single feed which contains individual articles
 */
public class Feed {
    String title;
    String url;
    String description;
    String copyright;
    String date;
    int priority;
    List<Article> articles = new ArrayList<Article>();

    public Feed(String url) {
        this.url = url;
    }

    public List<Article> pullFeed() {
        URL link;
        String itemTitle = "";
        String itemUrl = "";
        String itemDescription = "";
        boolean isItem = false;
        InputStream inputStream = null;
        List<Article> items = new ArrayList<Article>();

        // If the url does not already contain an http(s) at the beginning, add one.
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        // Initialize an inputStreamReader for the RSS feed.
        try {
            link = new URL(url);
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
                String name  = xmlPullParser.getName();

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

                Log.d("XmlParser", "Parsing name ==> " + name);

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
                        this.title = itemTitle;
                        this.url = itemUrl;
                        this.description = itemDescription;
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

        return items;
    }
}
