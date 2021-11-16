package monster.minions.binocularss.operations

import me.xdrop.fuzzywuzzy.FuzzySearch
import monster.minions.binocularss.dataclasses.Article

class ArticleSearchComparator(
    private var query: String
    ): Comparator<Article> {



    /**
     * Compare both articles using FuzzySearch ratio to determine which is a better
     * match to a user's query. Return a negative int, it p1 is a better match, 0 if
     * they are an equal match, and a positive int if p0 is a better match
     *
     * @param p0 The first article to compare
     * @param p1 The second article to compare
     */
    override fun compare(p0: Article?, p1: Article?): Int {

       val p0Ratio = FuzzySearch.tokenSetRatio(query, p0?.title)
       val p1Ratio = FuzzySearch.tokenSetRatio(query, p1?.title)

       return p0Ratio - p1Ratio
    }


}