package app.nighttrain.domain

import app.nighttrain.data.Deck
import app.nighttrain.data.Kind
import app.nighttrain.data.Moment
import app.nighttrain.data.Result
import app.nighttrain.data.Trip

/** What the travel log adds up to: where the train stands, where it has been, how well you guess each other. */
object Journal {
    /** Where the last finished trip arrived: the next one leaves from here. */
    fun city(trips: List<Trip>): String = trips.filter { it.done }.maxByOrNull { it.endedAt }?.to ?: Rails.HOME

    fun visited(trips: List<Trip>): Set<String> = trips.filter { it.done }.flatMap { listOf(it.from, it.to) }.toSet() + Rails.HOME

    /** Lines travelled, by [Rails.key], with how many times. */
    fun travelled(trips: List<Trip>): Map<String, Int> = trips.filter { it.done }.groupingBy { Rails.key(it.from, it.to) }.eachCount()

    class Summary(
        val questions: Int,
        val hits: Int,
        val near: Int,
        val misses: Int,
        val matches: Int,
        val dining: Int,
        val skipped: Int,
        val starred: Int,
    ) {
        val guesses: Int get() = hits + near + misses
    }

    fun summary(moments: List<Moment>): Summary = Summary(
        questions = moments.count { it.resultOf in setOf(Result.DONE, Result.HIT, Result.NEAR, Result.MISS) },
        hits = moments.count { it.resultOf == Result.HIT },
        near = moments.count { it.resultOf == Result.NEAR },
        misses = moments.count { it.resultOf == Result.MISS },
        matches = moments.count { it.resultOf == Result.MATCH },
        dining = moments.count { it.resultOf == Result.MATCH || it.resultOf == Result.SPLIT },
        skipped = moments.count { it.resultOf == Result.SKIPPED },
        starred = moments.count { it.starred },
    )

    /** Share of guesses that hit (a near miss counts half), or null with no guesses yet. */
    fun accuracy(moments: List<Moment>): Float? {
        val s = summary(moments)
        return if (s.guesses == 0) null else (s.hits + s.near * 0.5f) / s.guesses
    }

    /** How many of the conductor's questions have come up at least once, out of all of them. */
    fun coverage(deck: Deck, moments: List<Moment>, adult: Boolean): Pair<Int, Int> {
        val all = deck.cards.filter { it.kind == Kind.Q && (adult || !it.adult) }.map { it.id }.toSet()
        val asked = moments.map { it.cardId }.filter { it in all }.toSet()
        return asked.size to all.size
    }
}
