package monster.minions.binocularss.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import monster.minions.binocularss.dataclasses.Feed


class DatabaseGateway(private var context: Context) {

    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    init {
        this.setDb()
       this.setFeedDao()
    }

    /**
     * Get all feeds from the database
     */
    fun read(): MutableList<Feed>{
        return feedDao.getAll()
    }


    private fun setDb(){
        db = Room
            .databaseBuilder(context, AppDatabase::class.java, "feed-db")
            .allowMainThreadQueries()
            .build()

    }

    private fun setFeedDao(){
        feedDao = (db as AppDatabase).feedDao()
    }

    fun addFeeds(feedsToAdd: MutableList<Feed>){
        feedDao.insertAll(*(feedsToAdd.toTypedArray()))
    }

    fun removeFeedBySource(source: String){
       feedDao.deleteBySource(source)
    }
}