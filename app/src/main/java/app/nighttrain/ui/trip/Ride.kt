package app.nighttrain.ui.trip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.nighttrain.AppContainer
import app.nighttrain.data.Moment
import app.nighttrain.data.Result
import app.nighttrain.data.Trip
import app.nighttrain.domain.Director
import app.nighttrain.domain.Stop
import app.nighttrain.domain.StopType

/**
 * A trip in motion: which stop the train is at, and the little state of the card on the table (a
 * postcard being kept, a follow-up asked, the step of a guess). Everything said is written as it's done,
 * so the trip can be left at any point and picked up again.
 */
class Ride(private val container: AppContainer, trip: Trip, val stops: List<Stop>, recorded: List<Moment> = emptyList()) {
    val tripId = trip.id
    var pos by mutableIntStateOf(trip.pos.coerceIn(0, (stops.size - 1).coerceAtLeast(0)))
        private set
    private var tunnel = trip.tunnel
    private var sleeper = trip.sleeper

    // The card on the table.
    var starred by mutableStateOf(false)
    var note by mutableStateOf("")
    /** A follow-up the conductor added: "And deeper?". */
    var deeper by mutableStateOf<String?>(null)
    /** "Return ticket": the same question to the other passenger. */
    var swapped by mutableStateOf(false)
    /** Steps within a stop: a guess's reveal, the restaurant car's countdown. */
    var phase by mutableIntStateOf(0)
    /** Which card of a stop with several (restaurant car, fellow passenger). */
    var index by mutableIntStateOf(0)
    /** Both thumbs on the gate. */
    val holding = mutableStateListOf(false, false)
    var gate by mutableFloatStateOf(0f)
    /** Restaurant-car pairs that matched on this stop. */
    var matches by mutableIntStateOf(0)

    init {
        // Picked up mid-stop: the restaurant car or a fellow passenger carries on from the card it was at.
        val here = stops.getOrNull(pos)
        if (here != null && here.type in setOf(StopType.DINING, StopType.STRANGER)) {
            val done = recorded.filter { it.cardId in here.cards }
            index = done.map { it.cardId }.distinct().size
            matches = done.count { it.resultOf == Result.MATCH }
            if (index > 0 && here.type == StopType.STRANGER) phase = 1
        }
    }

    val stop: Stop get() = stops[pos]
    val last: Boolean get() = pos >= stops.lastIndex

    /** Who answers the current card, after a return ticket. */
    fun to(base: Int = stop.to): Int = if (swapped && base != 2) 1 - base else base

    private fun reset() {
        starred = false
        note = ""
        deeper = null
        swapped = false
        phase = 0
        index = 0
        holding[0] = false
        holding[1] = false
        gate = 0f
        matches = 0
    }

    /** Writes one card's outcome. */
    fun record(card: String, to: Int, result: Result, star: Boolean = starred, note: String = this.note) {
        if (card.isEmpty()) return
        val m = Moment(tripId = tripId, cardId = card, at = System.currentTimeMillis(), to = to, result = result.name, starred = star, note = note.trim())
        container.write { db.dao().insert(m) }
    }

    /** The next card within a stop that has several. */
    fun nextCard() {
        index++
        phase = 0
        starred = false
        note = ""
        deeper = null
        swapped = false
    }

    /** On to the next stop; a declined gate passes by everything behind it. */
    fun advance(decline: Boolean = false) {
        val here = stop
        var next = pos + 1
        if (here.type == StopType.TUNNEL) if (decline) next = skip(next, Director.ZONE_TUNNEL) else tunnel = true
        if (here.type == StopType.SLEEPER) if (decline) next = skip(next, Director.ZONE_SLEEPER) else sleeper = true
        if (here.type == StopType.NOTE) here.extra.toLongOrNull()?.let { id ->
            container.write {
                db.dao().notes().firstOrNull { it.id == id }?.let { db.dao().update(it.copy(readAt = System.currentTimeMillis())) }
            }
        }
        jump(next.coerceAtMost(stops.lastIndex))
    }

    /** Straight to the arrival: getting off at the next station. */
    fun terminus() = jump(stops.lastIndex)

    private fun jump(to: Int) {
        pos = to
        reset()
        val t = tunnel
        val s = sleeper
        container.write { db.dao().trip(tripId)?.let { db.dao().update(it.copy(pos = to, tunnel = t, sleeper = s)) } }
    }

    private fun skip(from: Int, zone: String): Int {
        var i = from
        while (i < stops.lastIndex && stops[i].zone == zone) i++
        return i
    }

    /** The trip ends: the train is at its destination. */
    fun arrive(onDone: () -> Unit) {
        container.write {
            db.dao().trip(tripId)?.let { db.dao().update(it.copy(endedAt = System.currentTimeMillis(), pos = stops.lastIndex)) }
        }
        onDone()
    }
}
