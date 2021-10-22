package monster.minions.binocularss

import java.net.URL

data class Feed(
    var title:String = "",
    var url:URL = URL(""),
    var description:String = "",
    var copyright:String = "",
    var date:String = "",
    var tags:List<String> = mutableListOf<String>(),
    var priority:Int = 0,
    var articles:List<Article> = mutableListOf<Article>()
)
