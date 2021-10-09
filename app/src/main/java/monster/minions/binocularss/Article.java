package monster.minions.binocularss;

public class Article {
    String title;
    String date;
    String url;
    String source;
    String author;
    String text;
    String description;

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
}
