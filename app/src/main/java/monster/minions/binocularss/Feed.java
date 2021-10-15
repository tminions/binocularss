package monster.minions.binocularss;

import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a single feed which contains individual articles
 */
public class Feed {
    private String title;
    private String url;
    private String description;
    private String copyright;
    private String date;
    private List<String> tags;
    int priority;
    List<Article> articles = new ArrayList<Article>();
    private Exception IllegalArgumentException;

    public Feed(String url) {
        // If the url does not already contain an http(s) at the beginning, add one.
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getDate() {
        return date;
    }

    public int getPriority() {
        return priority;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) throws Throwable {
        // If the url does not already contain an http(s) at the beginning, add one.
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        this.url = url;

        if (!URLUtil.isValidUrl(url)) {
            throw IllegalArgumentException;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    @NonNull
    @Override
    public String toString() {
        return "Feed{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", copyright='" + copyright + '\'' +
                ", date='" + date + '\'' +
                ", tags=" + tags +
                ", priority=" + priority +
                ", articles=" + articles +
                '}';
    }
}
