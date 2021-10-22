package monster.minions.binocularss

/**
 * from https://validator.w3.org/feed/docs/rss2.html
 */
data class Feed(
    var title: String = "", // The name of the channel. It's how people refer to your service. If you have an HTML website that contains the same information as your RSS file, the title of your channel should be the same as the title of your website.
    var link: String = "", // The URL to the HTML website corresponding to the channel.
    var description: String = "", // Phrase or sentence describing the channel.
    var language: String = "", // The language the channel is written in. This allows aggregators to group all Italian language sites, for example, on a single page.
    var copyright: String = "", // Copyright notice for content in the channel.
    var managingEditor: String = "", // Email address for person responsible for editorial content.
    var webMaster: String = "", // Email address for person responsible for technical issues relating to channel.
    var pubDate: String = "", // The publication date for the content in the channel. For example, the New York Times publishes on a daily basis, the publication date flips once every 24 hours. That's when the pubDate of the channel changes. All date-times in RSS conform to the Date and Time Specification of RFC 822, with the exception that the year may be expressed with two characters or four characters (four preferred).
    var lastBuildDate: String = "", // The last time the content of the channel changed.
    var category: String = "", // Specify one or more categories that the channel belongs to. Follows the same rules as the <item>-level category element.
    var generator: String = "", // A string indicating the program used to generate the channel.
    var docs: String = "", // A URL that points to the documentation for the format used in the RSS file. It's probably a pointer to this page. It's for people who might stumble across an RSS file on a Web server 25 years from now and wonder what it is.
    var cloud: String = "", // Allows processes to register with a cloud to be notified of updates to the channel, implementing a lightweight publish-subscribe protocol for RSS feeds.
    var ttl: String = "", // ttl stands for time to live. It's a number of minutes that indicates how long a channel can be cached before refreshing from the source.
    var image: String = "", // Specifies a GIF, JPEG or PNG image that can be displayed with the channel.
    var textInput: String = "", // Specifies a text input box that can be displayed with the channel.
    var skipHours: String = "", // A hint for aggregators telling them which hours they can skip.
    var skipDays: String = "", // A hint for aggregators telling them which days they can skip.
    var tags: List<String> = mutableListOf<String>(),
    var priority: Int = 0,
    var articles: List<Article> = mutableListOf<Article>()
)
