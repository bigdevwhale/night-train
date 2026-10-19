package app.nighttrain

import androidx.test.core.app.ApplicationProvider
import app.nighttrain.data.Backup
import app.nighttrain.data.Deck
import app.nighttrain.data.Everything
import app.nighttrain.data.Kind
import app.nighttrain.data.Length
import app.nighttrain.data.Moment
import app.nighttrain.data.Note
import app.nighttrain.data.Result
import app.nighttrain.data.Stage
import app.nighttrain.data.Trip
import app.nighttrain.domain.Biome
import app.nighttrain.domain.Director
import app.nighttrain.domain.Journal
import app.nighttrain.domain.Plan
import app.nighttrain.domain.Rails
import app.nighttrain.domain.StopType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NightTrainTest {
    private val deck: Deck by lazy { Deck.load(ApplicationProvider.getApplicationContext<android.app.Application>().assets) }

    @Test
    fun deckIsWellFormed() {
        val raw = ApplicationProvider.getApplicationContext<android.app.Application>().assets.let { a ->
            a.list(Deck.DIR)!!.filter { it.endsWith(".tsv") }.flatMap { f -> a.open("${Deck.DIR}/$f").use { it.readBytes().decodeToString() }.lines().filter { it.isNotBlank() } }
        }
        // Every line parses: nothing is silently dropped.
        assertEquals(raw.size, deck.cards.size)
        assertEquals("ids are unique", deck.cards.size, deck.cards.map { it.id }.toSet().size)
        deck.cards.forEach { c ->
            assertTrue(c.id, c.en.isNotBlank() && c.ru.isNotBlank())
            // The conductor says «вы»: no "(а)" left for a gender.
            assertFalse(c.id, c.ru.contains("(а)"))
            if (c.kind == Kind.D) {
                assertTrue(c.id, c.en.count { it == '|' } == 1 && c.ru.count { it == '|' } == 1)
            }
            if (c.kind == Kind.S) assertTrue(c.id, c.theme in Director.strangers)
            if (c.kind == Kind.Q) assertTrue(c.id, c.theme in Biome.entries.flatMap { it.themes } + "us")
        }
        Director.strangers.forEach { assertTrue(it, deck.stranger(it).size >= Director.strangerCount(it) * 3) }
        assertTrue(deck.of(Kind.F).size >= 20)
        assertTrue(deck.of(Kind.D).size >= 40)
        assertTrue(deck.of(Kind.H).size >= 20)
    }

    @Test
    fun everySubjectOfEveryLandscapeHasQuestions() {
        Biome.entries.flatMap { it.themes }.toSet().forEach { theme ->
            assertTrue(theme, deck.cards.count { it.kind == Kind.Q && it.theme == theme && !it.adult } >= 10)
        }
    }

    private fun plan(length: Length, seed: Long, history: List<Moment> = emptyList(), sleeper: Boolean = false, notes: List<Note> = emptyList()) =
        Director.plan(Director.Input(deck, length, Biome.FOREST, seed, history, sleeper, notes))

    @Test
    fun aPlanHasAllItsPartsAndNoCardTwice() {
        Length.entries.forEach { len ->
            repeat(20) { seed ->
                val stops = plan(len, seed.toLong())
                assertEquals(StopType.ARRIVAL, stops.last().type)
                assertEquals(1, stops.count { it.type == StopType.TUNNEL })
                assertTrue(stops.any { it.type == StopType.DINING })
                assertTrue(stops.any { it.type == StopType.STRANGER })
                val cards = stops.flatMap { it.cards }
                assertEquals("$len $seed", cards.size, cards.toSet().size)
                assertTrue(cards.all { deck[it] != null })
                // Stops behind the tunnel gate are the tunnel's, and only they are.
                val gate = stops.indexOfFirst { it.type == StopType.TUNNEL }
                val inside = stops.drop(gate + 1).takeWhile { it.zone == Director.ZONE_TUNNEL }
                assertTrue(inside.isNotEmpty())
                assertTrue(inside.all { it.stage == Stage.TUNNEL })
                // No sleeping car unless asked for.
                assertTrue(stops.none { it.type == StopType.SLEEPER })
                assertTrue(cards.none { deck[it]!!.adult })
            }
        }
        assertTrue(plan(Length.NIGHT, 1).size > plan(Length.LOCAL, 1).size)
    }

    @Test
    fun theSameSeedLaysTheSameTripAndAnotherADifferentOne() {
        assertEquals(plan(Length.EXPRESS, 7), plan(Length.EXPRESS, 7))
        assertTrue(plan(Length.EXPRESS, 7).flatMap { it.cards } != plan(Length.EXPRESS, 8).flatMap { it.cards })
    }

    @Test
    fun questionsDontComeBackUntilTheirStageRunsOut() {
        val history = mutableListOf<Moment>()
        var at = 0L
        // For each stage: how many different questions came up before the first one came back.
        val seen = mutableMapOf<Stage, MutableSet<String>>()
        val firstRepeat = mutableMapOf<Stage, Int>()
        repeat(30) { trip ->
            plan(Length.NIGHT, trip.toLong(), history).forEach { s ->
                s.cards.forEach { id ->
                    val card = deck[id]!!
                    if (card.kind == Kind.Q) {
                        val set = seen.getOrPut(card.stage) { mutableSetOf() }
                        if (id in set) firstRepeat.putIfAbsent(card.stage, set.size)
                        set += id
                    }
                    history += Moment(tripId = trip.toLong(), cardId = id, at = ++at, to = 0)
                }
            }
        }
        listOf(Stage.EVENING, Stage.NIGHT, Stage.TUNNEL, Stage.DAWN).forEach { stage ->
            val pool = deck.questions(stage).size
            val before = firstRepeat[stage] ?: pool
            // Guesses draw from the guessable part of a stage, which can run dry a little earlier.
            assertTrue("$stage: $before of $pool", before >= pool * 0.8)
        }
    }

    @Test
    fun theSleepingCarAndNotesComeOnlyWhenThere() {
        val stops = plan(Length.EXPRESS, 3, sleeper = true, notes = listOf(Note(id = 5, from = 0, text = "hi", createdAt = 1)))
        assertEquals(StopType.NOTE, stops.first().type)
        assertEquals(1, stops.first().to)
        assertEquals("5", stops.first().extra)
        val gate = stops.indexOfFirst { it.type == StopType.SLEEPER }
        assertTrue(gate > stops.indexOfFirst { it.type == StopType.TUNNEL })
        assertTrue(stops.filter { it.zone == Director.ZONE_SLEEPER }.all { deck[it.card]!!.adult })
        // A note already found isn't found again.
        assertTrue(plan(Length.EXPRESS, 3, notes = listOf(Note(id = 5, from = 0, text = "hi", createdAt = 1, readAt = 2))).none { it.type == StopType.NOTE })
    }

    @Test
    fun fellowPassengersTakeTurns() {
        val first = plan(Length.LOCAL, 11).first { it.type == StopType.STRANGER }
        val history = first.cards.map { Moment(tripId = 1, cardId = it, at = 1, to = 0) }
        repeat(10) { seed ->
            val next = plan(Length.LOCAL, 100L + seed, history).first { it.type == StopType.STRANGER }
            assertTrue(next.extra != first.extra)
        }
    }

    @Test
    fun aPostcardComesBackOnTheSameTrainAfterAMonth() {
        val q = deck.questions(Stage.NIGHT).first()
        val old = Moment(id = 9, tripId = 1, cardId = q.id, at = 0, to = 1, starred = true, note = "x")
        val echoed = (0 until 30).map { s -> Director.plan(Director.Input(deck, Length.EXPRESS, Biome.SEA, s.toLong(), listOf(old), now = 40L * 86_400_000L)) }
            .count { p -> p.any { it.type == StopType.ECHO && it.card == q.id && it.extra == "9" } }
        assertTrue(echoed > 10)
        // Not before a month is out, and not again once asked.
        val early = Director.plan(Director.Input(deck, Length.EXPRESS, Biome.SEA, 1, listOf(old), now = 5L * 86_400_000L))
        assertTrue(early.none { it.type == StopType.ECHO })
        val again = listOf(old, old.copy(id = 10, at = 10, starred = false))
        assertTrue((0 until 30).none { s -> Director.plan(Director.Input(deck, Length.EXPRESS, Biome.SEA, s.toLong(), again, now = 40L * 86_400_000L)).any { it.type == StopType.ECHO } })
    }

    @Test
    fun planSurvivesBeingStored() {
        Length.entries.forEach { len ->
            val stops = plan(len, 42, sleeper = true, notes = listOf(Note(id = 1, from = 1, text = "x", createdAt = 0)))
            assertEquals(stops, Plan.decode(Plan.encode(stops)))
        }
    }

    @Test
    fun theRailwayHangsTogether() {
        val keys = Rails.cities.map { it.key }.toSet()
        assertTrue(Rails.lines.all { it.a in keys && it.b in keys && it.a != it.b })
        assertEquals(Rails.lines.size, Rails.lines.map { Rails.key(it.a, it.b) }.toSet().size)
        // Every city can be reached from Midnight Junction.
        val reached = mutableSetOf(Rails.HOME)
        var frontier = listOf(Rails.HOME)
        while (frontier.isNotEmpty()) {
            frontier = frontier.flatMap { c -> Rails.departures(c).map { it.other(c) } }.filter { reached.add(it) }
        }
        assertEquals(keys, reached)
        assertTrue(Rails.cities.all { Rails.departures(it.key).size >= 2 })
    }

    @Test
    fun theLogKnowsWhereTheTrainStands() {
        val trips = listOf(
            Trip(id = 1, from = "midnight", to = "pines", seed = 1, length = "LOCAL", plan = "", startedAt = 1, endedAt = 2),
            Trip(id = 2, from = "pines", to = "owls", seed = 1, length = "LOCAL", plan = "", startedAt = 3, endedAt = 4),
            Trip(id = 3, from = "owls", to = "snow", seed = 1, length = "LOCAL", plan = "", startedAt = 5),
        )
        assertEquals("owls", Journal.city(trips))
        assertEquals(setOf("midnight", "pines", "owls"), Journal.visited(trips))
        assertEquals(Rails.HOME, Journal.city(emptyList()))
        val m = listOf(Result.HIT, Result.NEAR, Result.MISS, Result.MISS).map { Moment(tripId = 1, cardId = "x", at = 0, to = 0, result = it.name) }
        assertEquals(0.375f, Journal.accuracy(m)!!, 0.001f)
    }

    @Test
    fun backupRoundTrips() {
        val all = Everything(
            listOf(Trip(id = 3, from = "midnight", to = "pines", seed = -5, length = "NIGHT", plan = "QUESTION~E~pa-001~0~~~", pos = 1, startedAt = 10, endedAt = 20, tunnel = true)),
            listOf(Moment(id = 4, tripId = 3, cardId = "pa-001", at = 11, to = 2, result = Result.HIT.name, starred = true, note = "«Сирень»")),
            listOf(Note(id = 1, from = 1, text = "До встречи", createdAt = 5, readAt = 6)),
        )
        val back = Backup.decode(Backup.encode(all))
        assertEquals(all.trips, back.trips)
        assertEquals(all.moments, back.moments)
        assertEquals(all.notes, back.notes)
        assertNotNull(runCatching { Backup.decode("{}") }.exceptionOrNull())
    }
}
