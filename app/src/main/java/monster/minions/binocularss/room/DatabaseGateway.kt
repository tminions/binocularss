package monster.minions.binocularss.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import monster.minions.binocularss.dataclasses.Feed


class DatabaseGateway {

    private var db: RoomDatabase = Room
        .databaseBuilder(context, AppDatabase::class.java, "feed-db")
        .allowMainThreadQueries()
        .build()
    private var feedDao: FeedDao = (db as AppDatabase).feedDao()
    private lateinit var context: Context

    fun read(): MutableList<Feed>{
        return feedDao.getAll()
    }

    fun write(feedsToAdd: MutableList<Feed>){
        feedDao.insertAll(*(feedsToAdd.toTypedArray()))
    }
}