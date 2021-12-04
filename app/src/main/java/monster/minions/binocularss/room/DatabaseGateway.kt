package monster.minions.binocularss.room

import android.content.Context
import androidx.room.RoomDatabase
import monster.minions.binocularss.dataclasses.Feed


class DatabaseGateway {

    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao
    private lateinit var context: Context

    public fun read(){


    }

    public fun write(feedsToAdd: MutableList<Feed>){
        feedDao.insertAll(*(feedsToAdd.toTypedArray()))

    }
}