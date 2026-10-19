package app.nighttrain.data

import app.nighttrain.AppContainer
import app.nighttrain.domain.Director
import app.nighttrain.domain.Plan
import app.nighttrain.domain.Rails
import app.nighttrain.domain.StopType
import java.util.Locale
import kotlin.random.Random

/**
 * A made-up four months of trips for screenshots (`-Pdemo`): a couple who ride most weekends, getting
 * better at guessing each other, with a few postcards kept and a note waiting in the compartment.
 */
object DemoSeed {
    private const val DAY = 86_400_000L

    private val route = listOf("midnight", "pines", "owls", "bells", "harbour", "lights", "midnight", "mirror", "pass", "cinder", "mirror")

    private val kept = mapOf(
        "pa-001" to ("Tar on hot sleepers and grandma's lilac by the porch." to "Шпалы, нагретые солнцем, и бабушкина сирень у крыльца."),
        "pa-003" to ("Under the stairs at the dacha, with a torch and a stolen jar of jam." to "Под лестницей на даче, с фонариком и украденной банкой варенья."),
        "us-001" to ("The way you hold your cup with both hands, even in summer." to "Как ты держишь чашку двумя руками, даже летом."),
        "us-043" to ("The one who sings in the kitchen. Badly." to "Тот, кто поёт на кухне. Плохо."),
        "in-004" to ("Find your hand. Then the tea." to "Найти твою руку. Потом чай."),
    )

    suspend fun seedIfEmpty(c: AppContainer) {
        val dao = c.db.dao()
        if (dao.trips().isNotEmpty()) return
        val ru = Locale.getDefault().language == "ru"
        c.settings.update { it.copy(onboarded = true, nameA = if (ru) "Аня" else "Anna", nameB = if (ru) "Марк" else "Mark", sleeper = false) }

        val rng = Random(42)
        val now = System.currentTimeMillis()
        val history = mutableListOf<Moment>()
        val legs = route.zipWithNext()
        legs.forEachIndexed { i, (from, to) ->
            val start = now - (legs.size - i) * 11 * DAY - 20 * 3_600_000L
            val length = if (i % 3 == 1) Length.NIGHT else if (i % 3 == 2) Length.LOCAL else Length.EXPRESS
            val seed = rng.nextLong()
            val stops = Director.plan(Director.Input(c.deck, length, Rails.biome(from, to), seed, history.toList(), now = start))
            val id = dao.insert(
                Trip(
                    from = from, to = to, seed = seed, length = length.name, plan = Plan.encode(stops), pos = stops.lastIndex,
                    startedAt = start, endedAt = start + length.minutes * 60_000L, tunnel = rng.nextFloat() < 0.8f,
                )
            )
            // Later trips guess better: they've been listening.
            val skill = 0.3f + 0.5f * i / legs.size
            var at = start
            stops.forEach { s ->
                at += 3 * 60_000L
                suspend fun add(card: String, to: Int, r: Result, star: Boolean = false) {
                    val m = Moment(tripId = id, cardId = card, at = at, to = to, result = r.name, starred = star)
                    history += m.copy(id = dao.insert(m))
                }
                when (s.type) {
                    StopType.QUESTION, StopType.HALT -> add(s.card, s.to, if (rng.nextFloat() < 0.06f) Result.SKIPPED else Result.DONE, rng.nextFloat() < 0.08f)
                    StopType.GUESS -> add(s.card, s.to, rng.nextFloat().let { if (it < skill) Result.HIT else if (it < skill + 0.2f) Result.NEAR else Result.MISS })
                    StopType.DINING -> s.cards.forEach { add(it, 2, if (rng.nextFloat() < 0.55f) Result.MATCH else Result.SPLIT) }
                    StopType.STRANGER -> s.cards.forEachIndexed { k, card -> add(card, (s.to + k) % 2, Result.DONE) }
                    else -> Unit
                }
            }
        }
        // A few postcards with what was said, spread over the first trips.
        val trips = dao.trips().sortedBy { it.startedAt }
        kept.entries.forEachIndexed { i, (card, words) ->
            val trip = trips[i % trips.size]
            dao.insert(Moment(tripId = trip.id, cardId = card, at = trip.startedAt + (i + 2) * 600_000L, to = i % 2, starred = true, note = if (ru) words.second else words.first))
        }
        dao.insert(Note(from = 0, text = if (ru) "Сегодня ты так смеялся над моряком. Давай чаще ездить ночным." else "You laughed so hard at the sailor tonight. Let's take the night train more often.", createdAt = now - 2 * DAY))
    }
}
