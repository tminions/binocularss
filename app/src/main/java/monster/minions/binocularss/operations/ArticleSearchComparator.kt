package monster.minions.binocularss.operations

import android.util.Log
import me.xdrop.fuzzywuzzy.FuzzySearch
import monster.minions.binocularss.dataclasses.Article

class ArticleSearchComparator(
    private var query: String
    ): Comparator<Article> {



    /**
     * Compare both articles using FuzzySearch ratio to determine which is a better
     * match to a user's query. Return a negative int, it p0 is a better match, 0 if
     * they are an equal match, and a positive int if p1 is a better match
     *
     * @param p0 The first article to compare
     * @param p1 The second article to compare
     */
    override fun compare(p0: Article?, p1: Article?): Int {


       val p0Ratio = FuzzySearch.tokenSetRatio(query, p0?.title.toString())
       val p1Ratio = FuzzySearch.tokenSetRatio(query, p1?.title.toString())

       // Do p1Ration - p0Ratio because when we fill up the lazy column, we want
       // the articles with higher ratios to show up first and the list is in ascending order
       return p1Ratio - p0Ratio
    }


}