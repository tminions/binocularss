package monster.minions.binocularss.room

import androidx.room.Database
import androidx.room.RoomDatabase
import monster.minions.binocularss.dataclasses.Feed

/**
 * A class to call on the database
 */
@Database(entities = [Feed::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    /**
     * A feed Data Access Object (DAO)
     */
    abstract fun feedDao(): FeedDao
}