package monster.minions.binocularss

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Interface to translate from Kotlin function calls to SQLite queries
 */
@Dao
interface FeedDao {
    /**
     * Insert all feeds passed to it into the database.
     *
     * @param feeds An unspecified number of feeds.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg feeds: Feed)

    /**
     * Insert a single feed passed to it into the database.
     *
     * @param feed A feed object.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(feed: Feed)

    /**
     * Delete a Feed from the database
     *
     * @param feed A feed object.
     */
    @Delete
    fun delete(feed: Feed)

    /**
     * Get a mutable list of all the feeds from the database.
     *
     * @return A mutable list of Feed objects.
     */
    @Query("SELECT * FROM feeds")
    fun getAll(): MutableList<Feed>
}
