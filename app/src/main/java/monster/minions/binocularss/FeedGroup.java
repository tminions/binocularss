package monster.minions.binocularss;

import java.util.Collections;
import java.util.List;

public class FeedGroup {
    private List<Feed> feeds;

    public List<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public void addFeeds(Feed... feeds) {
        Collections.addAll(this.feeds, feeds);
    }

    public void removeFeeds(Feed... feeds) {
        for (Feed feed : feeds) {
            if (this.feeds.contains(feed)) {
                this.feeds.remove(feed);
            }
        }
    }
}
