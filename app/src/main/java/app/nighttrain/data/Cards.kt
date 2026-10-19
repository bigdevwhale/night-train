package app.nighttrain.data

import android.content.res.AssetManager

/** What a card asks for. */
enum class Kind {
    /** The conductor's question to one passenger (or both). */
    Q,

    /** A fellow passenger's question, in their own voice. */
    S,

    /** A whistle-stop: something to do, not to say. */
    H,

    /** A restaurant-car "this or that": "Mountains | Sea". */
    D,

    /** "And deeper?": a follow-up that fits any answer. */
    F,
}

/** How far into the night a card belongs. */
enum class Stage(val code: Char) {
    /** Boarding, the evening: light and playful. */
    EVENING('E'),

    /** The night: personal. */
    NIGHT('N'),

    /** The tunnel: the most open, in the dark. */
    TUNNEL('T'),

    /** Dawn: warm, the two of you, what's next. */
    DAWN('D'),

    /** Any time. */
    ANY('*');

    companion object {
        fun of(c: Char): Stage? = entries.firstOrNull { it.code == c }
    }
}

/**
 * One card of the deck. [id] is stable across versions and languages: history and postcards point at
 * it. [theme] is a question's subject, or a fellow passenger's key for [Kind.S].
 */
data class Card(
    val id: String,
    val kind: Kind,
    val stage: Stage,
    val theme: String,
    /** The other passenger can try to guess the answer first. */
    val guess: Boolean,
    /** Both answer, one after the other. */
    val both: Boolean,
    /** Only in the sleeping car. */
    val adult: Boolean,
    val en: String,
    val ru: String,
) {
    fun text(lang: String): String = if (lang == "ru") ru else en

    /** A restaurant-car pair split into its two sides. */
    fun sides(lang: String): Pair<String, String> {
        val t = text(lang)
        val i = t.indexOf('|')
        return if (i < 0) t to "" else t.substring(0, i).trim() to t.substring(i + 1).trim()
    }
}

/** Every card, read from the tab-separated files in assets/cards: id, kind, stage, theme, flags, en, ru. */
class Deck(val cards: List<Card>) {
    private val byId = cards.associateBy { it.id }
    operator fun get(id: String): Card? = byId[id]

    fun of(kind: Kind): List<Card> = cards.filter { it.kind == kind }

    /** Plain conductor's questions; the sleeping car's only when [adult]. */
    fun questions(stage: Stage, adult: Boolean = false): List<Card> =
        cards.filter { it.kind == Kind.Q && it.stage == stage && it.adult == adult }

    fun stranger(key: String): List<Card> = cards.filter { it.kind == Kind.S && it.theme == key }

    companion object {
        const val DIR = "cards"

        fun load(assets: AssetManager): Deck {
            val files = assets.list(DIR).orEmpty().filter { it.endsWith(".tsv") }.sorted()
            return Deck(files.flatMap { f -> assets.open("$DIR/$f").use { parse(it.readBytes().decodeToString()) } })
        }

        /** Lines that don't have all seven fields, or an unknown kind or stage, are skipped. */
        fun parse(text: String): List<Card> = text.lineSequence().mapNotNull { line ->
            val f = line.trimEnd('\r').split('\t')
            if (f.size != 7) return@mapNotNull null
            val kind = runCatching { Kind.valueOf(f[1].trim()) }.getOrNull() ?: return@mapNotNull null
            val stage = f[2].trim().singleOrNull()?.let(Stage::of) ?: return@mapNotNull null
            val flags = f[4].split(',').map { it.trim() }.toSet()
            Card(
                id = f[0].trim(), kind = kind, stage = stage, theme = f[3].trim(),
                guess = "g" in flags, both = "b" in flags, adult = "x" in flags,
                en = f[5].trim(), ru = f[6].trim(),
            )
        }.toList()
    }
}
