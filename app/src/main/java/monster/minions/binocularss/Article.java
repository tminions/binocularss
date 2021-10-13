package monster.minions.binocularss;

public class Article {
    private String title;
    private String date;
    private String url;
    private String source;
    private String author;
    private String text;
    private String description;

    public Article(String title, String date, String url, String source, String author, String text, String description) {
        this.title = title;
        this.date = date;
        this.url = url;
        this.source = source;
        this.author = author;
        this.text = text;
        this.description = description;
    }

    public Article(String title, String url, String description) {
        this.title = title;
        this.url = url;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
