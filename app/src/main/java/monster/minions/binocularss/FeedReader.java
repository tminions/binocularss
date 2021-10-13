package monster.minions.binocularss;

import java.util.ArrayList;
import java.util.List;

public class FeedReader {
    /**
     * Return the a list of a text summaries of the feeds in feedGroup.
     *
     * @param feedGroup A group of feeds.
     * @return A list of text summaries fo the feeds in feedGroup.
     */
    public List<String> readFeedGroup(FeedGroup feedGroup) {
        List<String> feedsTextList = new ArrayList<String>();

        for (Feed feed : feedGroup.getFeeds()) {
            feedsTextList.add(feed.toString());
        }
        return feedsTextList;
    }
}
