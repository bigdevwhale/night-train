package app.nighttrain.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** How a card went. */
enum class Result {
    /** Answered, done, eaten. */
    DONE,

    /** The emergency brake: passed over without a word. */
    SKIPPED,

    /** Guessed right: a hole punched in the ticket. */
    HIT,
    NEAR,
    MISS,

    /** Restaurant car: both pointed the same way. */
    MATCH,
    SPLIT,
}

/**
 * One trip from [from] to [to]. [plan] is the whole route of cards (see [app.nighttrain.domain.Plan]),
 * made at boarding so the trip can be picked up again; [pos] is how far along it the train is.
 */
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val from: String,
    val to: String,
    val seed: Long,
    /** [Length] name. */
    val length: String,
    val plan: String,
    val pos: Int = 0,
    val startedAt: Long,
    /** 0 while the train is still moving. */
    val endedAt: Long = 0,
    /** Went through the tunnel rather than around it. */
    val tunnel: Boolean = false,
    /** Opened the sleeping car. */
    val sleeper: Boolean = false,
) {
    val done: Boolean get() = endedAt > 0
    val lengthOf: Length get() = runCatching { Length.valueOf(length) }.getOrDefault(Length.EXPRESS)
}

/** The three trains, by how many stops they make. */
enum class Length(val stops: Int, val minutes: Int) {
    LOCAL(10, 25),
    EXPRESS(18, 60),
    NIGHT(28, 120),
}

/**
 * A card that came up on a trip and how it went. [to] is who answered: 0 or 1 for a passenger, 2 for
 * both. A starred moment is a postcard: kept, with an optional [note] of what was said.
 */
@Entity(tableName = "moments", indices = [Index("tripId"), Index("cardId")])
data class Moment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val cardId: String,
    val at: Long,
    val to: Int,
    /** [Result] name. */
    val result: String = Result.DONE.name,
    val starred: Boolean = false,
    val note: String = "",
) {
    val resultOf: Result get() = runCatching { Result.valueOf(result) }.getOrDefault(Result.DONE)
}

/** A note left in the compartment by one passenger for the other, found at the next boarding. */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 0 or 1: who wrote it; it's for the other one. */
    val from: Int,
    val text: String,
    val createdAt: Long,
    /** When it was found; 0 while it waits. */
    val readAt: Long = 0,
)

@Dao
abstract class TrainDao {
    @Query("SELECT * FROM trips ORDER BY startedAt DESC") abstract fun observeTrips(): Flow<List<Trip>>
    @Query("SELECT * FROM moments ORDER BY at ASC") abstract fun observeMoments(): Flow<List<Moment>>
    @Query("SELECT * FROM notes ORDER BY createdAt ASC") abstract fun observeNotes(): Flow<List<Note>>

    @Query("SELECT * FROM trips ORDER BY startedAt DESC") abstract suspend fun trips(): List<Trip>
    @Query("SELECT * FROM moments ORDER BY at ASC") abstract suspend fun moments(): List<Moment>
    @Query("SELECT * FROM notes ORDER BY createdAt ASC") abstract suspend fun notes(): List<Note>
    @Query("SELECT * FROM trips WHERE id = :id") abstract suspend fun trip(id: Long): Trip?

    @Insert abstract suspend fun insert(t: Trip): Long
    @Update abstract suspend fun update(t: Trip)
    @Insert abstract suspend fun insert(m: Moment): Long
    @Update abstract suspend fun update(m: Moment)
    @Insert abstract suspend fun insert(n: Note): Long
    @Update abstract suspend fun update(n: Note)
    @Delete abstract suspend fun delete(n: Note)

    @Query("DELETE FROM trips WHERE id = :id") protected abstract suspend fun deleteTripRow(id: Long)
    @Query("DELETE FROM moments WHERE tripId = :id") protected abstract suspend fun deleteMomentsOf(id: Long)

    /** A trip and everything said on it. */
    @Transaction
    open suspend fun deleteTrip(id: Long) {
        deleteMomentsOf(id); deleteTripRow(id)
    }

    @Query("DELETE FROM trips") protected abstract suspend fun clearTrips()
    @Query("DELETE FROM moments") protected abstract suspend fun clearMoments()
    @Query("DELETE FROM notes") protected abstract suspend fun clearNotes()
    @Insert protected abstract suspend fun insertTrips(l: List<Trip>)
    @Insert protected abstract suspend fun insertMoments(l: List<Moment>)
    @Insert protected abstract suspend fun insertNotes(l: List<Note>)

    @Transaction
    open suspend fun replaceAll(all: Everything) {
        clearTrips(); clearMoments(); clearNotes()
        insertTrips(all.trips); insertMoments(all.moments); insertNotes(all.notes)
    }
}

/** The whole travel log, for backups. */
class Everything(val trips: List<Trip>, val moments: List<Moment>, val notes: List<Note>)

@Database(entities = [Trip::class, Moment::class, Note::class], version = 1, exportSchema = true)
abstract class TrainDatabase : RoomDatabase() {
    abstract fun dao(): TrainDao

    companion object {
        fun create(context: Context): TrainDatabase =
            Room.databaseBuilder(context.applicationContext, TrainDatabase::class.java, "nighttrain.db").build()
    }
}
