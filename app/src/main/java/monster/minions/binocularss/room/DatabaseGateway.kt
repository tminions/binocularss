package monster.minions.binocularss.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import monster.minions.binocularss.dataclasses.Feed


class DatabaseGateway(private var context: Context) {

    /**
     * This class is meant acts as an interface between
     * the UI and the database in order to reduce coupling within
     * the outermost layer
     */

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

    /**
     * Initialize the database, make this method private since database
     * should only need to be set once in the initializer.
     */
    private fun setDb(){
        db = Room
            .databaseBuilder(context, AppDatabase::class.java, "feed-db")
            .allowMainThreadQueries()
            .build()

    }

    /**
     * Initialize the Data Access Object for Feeds
      */
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