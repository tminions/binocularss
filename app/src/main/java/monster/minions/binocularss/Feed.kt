package monster.minions.binocularss

data class Feed(
    var title:String,
    var url:String,
    var description:String,
    var copyright:String,
    var date:String,
    var tags:List<String>,
    var priority:Int,
    var articles:List<Article>
)
