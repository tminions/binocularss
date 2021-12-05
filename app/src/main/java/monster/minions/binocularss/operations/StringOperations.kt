package monster.minions.binocularss.operations

/**
 * Function to append https:// if the string does not have the prefix http:// or https://.
 *
 * @param url String to have https:// appended to it.
 * @return url with https:// possibly appended to it.
 */
fun addHttps(url: String): String {
    return when {
        url.startsWith("https://") || url.startsWith("http://") -> url
        else -> "https://$url"
    }
}

/**
 * Function to trim any whitespace characters
 *
 * @param url String with (or perhaps without) untrimmed whitespace.
 * @return url that has been stripped of its trailing and leading whitespace.
 */
fun trimWhitespace(url: String): String {
    return url.trim(' ', '\n', '\t', '\r')
}