package app.nighttrain.domain

import app.nighttrain.data.Card
import app.nighttrain.data.Deck
import app.nighttrain.data.Kind
import app.nighttrain.data.Length
import app.nighttrain.data.Moment
import app.nighttrain.data.Note
import app.nighttrain.data.Stage
import kotlin.random.Random

/** What happens at one stop of the trip. */
enum class StopType {
    /** A note left last time, found in the compartment. [Stop.to] is who it's for, [Stop.extra] the note's id. */
    NOTE,

    /** The conductor asks. */
    QUESTION,

    /** Guess first: the other passenger says what [Stop.to] will answer, then [Stop.to] answers. */
    GUESS,

    /** A station with a rule for the next few questions. */
    RULE,

    /** A whistle-stop: something to do. [Stop.extra] seeds the halt's name. */
    HALT,

    /** The restaurant car: five "this or that", both point at once. */
    DINING,

    /** A fellow passenger sits down for a few questions. [Stop.extra] is who. */
    STRANGER,

    /** The tunnel ahead: both hold on to go in, or the train goes round. */
    TUNNEL,

    /** The sleeping car (18+): the same, both hold on to open it. */
    SLEEPER,

    /** "The same train": a postcard from a past trip asked again. [Stop.extra] is the moment's id. */
    ECHO,

    /** Which answer on the crooked stretch wasn't true? */
    LIE,

    ARRIVAL,
}

/** A station's rule, for the next [Director.RULE_SPAN] questions. */
enum class Rule {
    /** Answer as the person opposite would, then they put it right. */
    MIRROR,

    /** Three words first, then the story. */
    THREE_WORDS,

    /** As you'd have answered at ten, and then as now. */
    CHILDHOOD,
    WHISPER,

    /** Both answer every question. */
    BOTH,

    /** Hold each other's eyes while answering. */
    EYES,
    HANDS,

    /** Ten seconds of quiet before each answer. */
    SILENCE,

    /** One answer on this stretch will be untrue. Who lies is a secret. */
    LIE,
}

/**
 * One stop. [cards] are deck ids; [to] is who answers (0, 1, or 2 for both); [zone] marks stops behind a
 * gate ("tunnel", "sleeper") that are passed by when the gate is declined.
 */
data class Stop(
    val type: StopType,
    val stage: Stage,
    val cards: List<String> = emptyList(),
    val to: Int = 0,
    val rule: Rule? = null,
    val extra: String = "",
    val zone: String = "",
) {
    val card: String get() = cards.firstOrNull().orEmpty()
}

/** A trip's stops, stored with the trip as text so it can be picked up where it was left. */
object Plan {
    fun encode(stops: List<Stop>): String = stops.joinToString("\n") { s ->
        listOf(s.type.name, s.stage.code.toString(), s.cards.joinToString(","), s.to.toString(), s.rule?.name.orEmpty(), s.extra, s.zone)
            .joinToString("~")
    }

    fun decode(text: String): List<Stop> = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
        val f = line.split('~')
        if (f.size < 7) return@mapNotNull null
        Stop(
            type = runCatching { StopType.valueOf(f[0]) }.getOrNull() ?: return@mapNotNull null,
            stage = f[1].singleOrNull()?.let(Stage::of) ?: Stage.NIGHT,
            cards = f[2].split(',').filter { it.isNotEmpty() },
            to = f[3].toIntOrNull() ?: 0,
            rule = f[4].takeIf { it.isNotEmpty() }?.let { r -> runCatching { Rule.valueOf(r) }.getOrNull() },
            extra = f[5],
            zone = f[6],
        )
    }
}

/**
 * Lays out a trip. Everything comes from the seed and the travel log, so no two trips run the same:
 * the order of the stops, which station rules come up, who sits down in the compartment, and above
 * all the cards, which are the ones never asked before, then the ones asked longest ago, tilted toward
 * the subjects of the line's landscape.
 */
object Director {
    const val RULE_SPAN = 3
    const val ZONE_TUNNEL = "tunnel"
    const val ZONE_SLEEPER = "sleeper"
    const val DINING_PAIRS = 5

    /** Fellow passengers, by deck key. */
    val strangers = listOf("sailor", "child", "seer", "poet", "inspector", "granny", "traveller", "monk")

    /** How many questions each fellow passenger asks; the inspector fires off four. */
    fun strangerCount(key: String): Int = when (key) {
        "inspector" -> 4
        else -> 2
    }

    // Q a question, G guess, R a station rule, H a halt, D the restaurant car, S a fellow passenger.
    private val templates = mapOf(
        Length.LOCAL to listOf("Q G Q D", "R Q G S Q", "Q Q", "G Q"),
        Length.EXPRESS to listOf("Q G Q R Q H Q D", "S Q G R Q Q G Q", "Q Q Q", "Q G H Q"),
        Length.NIGHT to listOf("Q G Q R Q Q H G D Q", "S Q G Q R Q Q G H Q S", "Q Q Q Q", "Q G R Q H Q"),
    )
    private val phases = listOf(Stage.EVENING, Stage.NIGHT, Stage.TUNNEL, Stage.DAWN)

    class Input(
        val deck: Deck,
        val length: Length,
        val biome: Biome,
        val seed: Long,
        /** Moments of every past trip: what has been asked, and when. */
        val history: List<Moment> = emptyList(),
        /** Offer the sleeping car after the tunnel. */
        val sleeper: Boolean = false,
        /** Notes waiting to be found. */
        val notes: List<Note> = emptyList(),
        /** Now, for picking postcards old enough to ask again. */
        val now: Long = System.currentTimeMillis(),
    )

    fun plan(input: Input): List<Stop> {
        val rng = Random(input.seed)
        val picker = Picker(input.deck, input.history, input.biome, rng)
        val stops = mutableListOf<Stop>()
        input.notes.filter { it.readAt == 0L }.forEach { stops += Stop(StopType.NOTE, Stage.EVENING, to = 1 - it.from, extra = it.id.toString()) }

        var turn = rng.nextInt(2)
        fun nextTurn(): Int = turn.also { turn = 1 - turn }
        val rules = Rule.entries.shuffled(rng).toMutableList()
        // The two fellow passengers met most recently sit this trip out.
        val recent = input.history.sortedByDescending { it.at }
            .mapNotNull { m -> input.deck[m.cardId]?.takeIf { it.kind == Kind.S }?.theme }.distinct().take(2)
        val strangers = (strangers.filter { it !in recent }.shuffled(rng) + recent).toMutableList()
        val templ = templates.getValue(input.length)

        phases.forEachIndexed { pi, stage ->
            val tokens = shuffleLightly(templ[pi].split(' '), rng)
            val zone = if (stage == Stage.TUNNEL) ZONE_TUNNEL else ""
            // The gate stands before the tunnel mouth, still under the night sky.
            if (stage == Stage.TUNNEL) stops += Stop(StopType.TUNNEL, Stage.NIGHT)
            var rule: Rule? = null
            var left = 0
            tokens.forEach { t ->
                when (t) {
                    "Q", "G" -> {
                        val guess = t == "G" || (stage != Stage.TUNNEL && rng.nextFloat() < 0.12f)
                        val card = if (guess && stage != Stage.TUNNEL) picker.guess(stage) else null
                        if (card != null) {
                            stops += Stop(StopType.GUESS, stage, listOf(card.id), nextTurn(), zone = zone)
                        } else {
                            val q = picker.question(stage) ?: return@forEach
                            val active = if (left > 0) rule else null
                            val to = if (q.both || active == Rule.BOTH) 2 else nextTurn()
                            stops += Stop(StopType.QUESTION, stage, listOf(q.id), to, active, zone = zone)
                            if (left > 0) {
                                left--
                                if (left == 0 && rule == Rule.LIE) stops += Stop(StopType.LIE, stage, zone = zone)
                            }
                        }
                    }
                    "R" -> if (rules.isNotEmpty()) {
                        rule = rules.removeAt(0)
                        left = RULE_SPAN
                        stops += Stop(StopType.RULE, stage, rule = rule, extra = rng.nextInt(1_000).toString(), zone = zone)
                    }
                    "H" -> picker.halt(stage)?.let { stops += Stop(StopType.HALT, stage, listOf(it.id), 2, extra = rng.nextInt(1_000).toString(), zone = zone) }
                    "D" -> picker.dining(DINING_PAIRS).takeIf { it.isNotEmpty() }?.let { stops += Stop(StopType.DINING, stage, it.map(Card::id), 2, zone = zone) }
                    "S" -> {
                        val key = strangers.firstOrNull { picker.stranger(it, 1).isNotEmpty() }
                        if (key != null) {
                            strangers.remove(key)
                            val cards = picker.stranger(key, strangerCount(key))
                            stops += Stop(StopType.STRANGER, stage, cards.map(Card::id), nextTurn(), extra = key, zone = zone)
                        }
                    }
                }
            }
            // A rule whose stretch ran out of questions still owes its reveal.
            if (left in 1 until RULE_SPAN && rule == Rule.LIE) stops += Stop(StopType.LIE, stage, zone = zone)

            if (stage == Stage.TUNNEL && input.sleeper) {
                val adult = picker.adult(3)
                if (adult.isNotEmpty()) {
                    stops += Stop(StopType.SLEEPER, Stage.TUNNEL)
                    adult.forEach { stops += Stop(StopType.QUESTION, Stage.TUNNEL, listOf(it.id), if (it.both) 2 else nextTurn(), zone = ZONE_SLEEPER) }
                }
            }
        }

        // The same train: one postcard from at least a month ago, asked again before dawn ends.
        echo(input, rng)?.let { m ->
            val at = stops.indexOfLast { it.stage == Stage.DAWN && it.type == StopType.QUESTION }
            val stop = Stop(StopType.ECHO, Stage.DAWN, listOf(m.cardId), if (m.to == 2) 2 else m.to, extra = m.id.toString())
            if (at >= 0) stops.add(at, stop) else stops += stop
        }
        stops += Stop(StopType.ARRIVAL, Stage.DAWN)
        return stops
    }

    /** A starred moment old enough to ask again, from among the oldest; never one already asked again since. */
    private fun echo(input: Input, rng: Random): Moment? {
        val month = 30L * 86_400_000L
        val lastAsked = input.history.groupBy { it.cardId }.mapValues { (_, l) -> l.maxOf { it.at } }
        val old = input.history.filter {
            it.starred && input.now - it.at >= month && lastAsked[it.cardId] == it.at && input.deck[it.cardId] != null
        }
        if (old.isEmpty() || rng.nextFloat() > 0.75f) return null
        return old.sortedBy { it.at }.take(5).random(rng)
    }

    /** Swaps a few neighbours, so the same template never plays twice the same way. */
    private fun shuffleLightly(tokens: List<String>, rng: Random): List<String> {
        val l = tokens.toMutableList()
        repeat(l.size / 2) {
            val i = rng.nextInt(l.size - 1)
            // A rule keeps something after it to apply to.
            if (l[i + 1] == "R" && i + 1 == l.lastIndex) return@repeat
            l[i] = l[i + 1].also { l[i + 1] = l[i] }
        }
        if (l.lastOrNull() == "R") l.add(0, l.removeAt(l.lastIndex))
        return l
    }

    /** Cards for one plan: each used once, the never-asked first, then the longest unasked, favouring the line's subjects. */
    private class Picker(val deck: Deck, history: List<Moment>, biome: Biome, val rng: Random) {
        private val used = mutableSetOf<String>()
        private val last: Map<String, Long> = history.groupBy { it.cardId }.mapValues { (_, l) -> l.maxOf { it.at } }
        private val favoured = biome.themes.toSet()

        fun pick(candidates: List<Card>, n: Int = 1): List<Card> {
            val free = candidates.filter { it.id !in used }
            if (free.isEmpty()) return emptyList()
            val fresh = free.filter { it.id !in last }
            val pool = fresh.ifEmpty { free.sortedBy { last[it.id] ?: 0L }.let { it.take(maxOf(n, it.size / 3)) } }.toMutableList()
            val out = mutableListOf<Card>()
            repeat(n) {
                if (pool.isEmpty()) return@repeat
                val weights = pool.map { if (it.theme in favoured) 3.0 else 1.0 }
                var r = rng.nextDouble() * weights.sum()
                var i = 0
                while (i < pool.lastIndex && r >= weights[i]) { r -= weights[i]; i++ }
                out += pool.removeAt(i)
            }
            used += out.map { it.id }
            return out
        }

        fun question(stage: Stage): Card? = pick(deck.questions(stage)).firstOrNull()
            ?: pick(deck.questions(Stage.NIGHT)).firstOrNull()

        /**
         * A guessable question, only a fresh one while the stage still has fresh questions of any kind:
         * guessables are a part of the stage and would otherwise come round again first.
         */
        fun guess(stage: Stage): Card? {
            val all = deck.questions(stage)
            val guessable = all.filter { it.guess && !it.both }
            val stageFresh = all.any { it.id !in last && it.id !in used }
            return pick(if (stageFresh) guessable.filter { it.id !in last } else guessable).firstOrNull()
        }

        fun halt(stage: Stage): Card? = pick(deck.of(Kind.H).filter { it.stage == stage || it.stage == Stage.ANY }).firstOrNull()
            ?: pick(deck.of(Kind.H)).firstOrNull()

        fun dining(n: Int): List<Card> = pick(deck.of(Kind.D), n)

        fun stranger(key: String, n: Int): List<Card> {
            val cards = deck.stranger(key).filter { it.id !in used }
            return if (cards.size < n) emptyList() else pick(cards, n)
        }

        fun adult(n: Int): List<Card> = pick(deck.cards.filter { it.kind == Kind.Q && it.adult }, n)
    }
}
