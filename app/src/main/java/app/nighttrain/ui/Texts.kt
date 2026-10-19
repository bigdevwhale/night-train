package app.nighttrain.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.nighttrain.R
import app.nighttrain.data.Length
import app.nighttrain.data.Settings
import app.nighttrain.domain.Biome
import app.nighttrain.domain.Rule
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Words, names and dates in the app's language. */
object T {
    private fun locale(c: Context): Locale = c.resources.configuration.locales[0]

    /** "ru" or "en": which side of a card to read. */
    fun lang(c: Context): String = if (locale(c).language == "ru") "ru" else "en"

    fun plural(c: Context, id: Int, n: Int): String = c.resources.getQuantityString(id, n, n)

    /** "17 Oct 2026". */
    fun date(c: Context, ms: Long): String =
        Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy", locale(c)))

    /** "17.10.26 · 21:40", as stamped on a ticket. */
    fun stamp(ms: Long): String =
        Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yy · HH:mm"))

    fun passenger(c: Context, s: Settings, who: Int): String =
        s.name(who, c.getString(R.string.passenger_a), c.getString(R.string.passenger_b))

    /** "Anna", "Boris" or "Both of you". */
    fun to(c: Context, s: Settings, who: Int): String = if (who == 2) c.getString(R.string.to_both) else passenger(c, s, who)

    private val cities = mapOf(
        "midnight" to R.string.city_midnight, "pines" to R.string.city_pines, "owls" to R.string.city_owls,
        "snow" to R.string.city_snow, "aurora" to R.string.city_aurora, "mirror" to R.string.city_mirror,
        "pass" to R.string.city_pass, "cinder" to R.string.city_cinder, "dawn" to R.string.city_dawn,
        "wormwood" to R.string.city_wormwood, "starfall" to R.string.city_starfall, "vines" to R.string.city_vines,
        "lights" to R.string.city_lights, "tide" to R.string.city_tide, "harbour" to R.string.city_harbour,
        "bells" to R.string.city_bells,
    )

    fun city(c: Context, key: String): String = c.getString(cities[key] ?: R.string.city_midnight)

    fun biome(c: Context, b: Biome): String = c.getString(
        when (b) {
            Biome.FOREST -> R.string.biome_forest
            Biome.SEA -> R.string.biome_sea
            Biome.LAKES -> R.string.biome_lakes
            Biome.MOUNTAINS -> R.string.biome_mountains
            Biome.STEPPE -> R.string.biome_steppe
            Biome.SNOW -> R.string.biome_snow
            Biome.CITY -> R.string.biome_city
            Biome.FIELDS -> R.string.biome_fields
        }
    )

    fun theme(c: Context, key: String): String = c.getString(
        when (key) {
            "childhood" -> R.string.th_childhood
            "family" -> R.string.th_family
            "past" -> R.string.th_past
            "quirks" -> R.string.th_quirks
            "senses" -> R.string.th_senses
            "fears" -> R.string.th_fears
            "dreams" -> R.string.th_dreams
            "values" -> R.string.th_values
            "work" -> R.string.th_work
            "future" -> R.string.th_future
            "imagination" -> R.string.th_imagination
            else -> R.string.th_us
        }
    )

    /** "childhood · the past · family". */
    fun themes(c: Context, b: Biome): String = b.themes.joinToString(" · ") { theme(c, it) }

    fun length(c: Context, l: Length): String = c.getString(
        when (l) {
            Length.LOCAL -> R.string.len_local
            Length.EXPRESS -> R.string.len_express
            Length.NIGHT -> R.string.len_night
        }
    )

    fun lengthSub(c: Context, l: Length): String = c.getString(
        when (l) {
            Length.LOCAL -> R.string.len_local_sub
            Length.EXPRESS -> R.string.len_express_sub
            Length.NIGHT -> R.string.len_night_sub
        }
    )

    fun ruleStation(c: Context, r: Rule): String = c.getString(
        when (r) {
            Rule.MIRROR -> R.string.rs_mirror
            Rule.THREE_WORDS -> R.string.rs_three
            Rule.CHILDHOOD -> R.string.rs_child
            Rule.WHISPER -> R.string.rs_whisper
            Rule.BOTH -> R.string.rs_both
            Rule.EYES -> R.string.rs_eyes
            Rule.HANDS -> R.string.rs_hands
            Rule.SILENCE -> R.string.rs_silence
            Rule.LIE -> R.string.rs_lie
        }
    )

    fun rule(c: Context, r: Rule): String = c.getString(
        when (r) {
            Rule.MIRROR -> R.string.rule_mirror
            Rule.THREE_WORDS -> R.string.rule_three
            Rule.CHILDHOOD -> R.string.rule_child
            Rule.WHISPER -> R.string.rule_whisper
            Rule.BOTH -> R.string.rule_both
            Rule.EYES -> R.string.rule_eyes
            Rule.HANDS -> R.string.rule_hands
            Rule.SILENCE -> R.string.rule_silence
            Rule.LIE -> R.string.rule_lie
        }
    )

    /** The rule as a reminder on each card it applies to. */
    fun ruleShort(c: Context, r: Rule): String = c.getString(
        when (r) {
            Rule.MIRROR -> R.string.rule_mirror_short
            Rule.THREE_WORDS -> R.string.rule_three_short
            Rule.CHILDHOOD -> R.string.rule_child_short
            Rule.WHISPER -> R.string.rule_whisper_short
            Rule.BOTH -> R.string.rule_both_short
            Rule.EYES -> R.string.rule_eyes_short
            Rule.HANDS -> R.string.rule_hands_short
            Rule.SILENCE -> R.string.rule_silence_short
            Rule.LIE -> R.string.rule_lie_short
        }
    )

    fun stranger(c: Context, key: String): String = c.getString(
        when (key) {
            "sailor" -> R.string.st_sailor
            "child" -> R.string.st_child
            "seer" -> R.string.st_seer
            "poet" -> R.string.st_poet
            "inspector" -> R.string.st_inspector
            "granny" -> R.string.st_granny
            "traveller" -> R.string.st_traveller
            else -> R.string.st_monk
        }
    )

    fun strangerIntro(c: Context, key: String): String = c.getString(
        when (key) {
            "sailor" -> R.string.st_sailor_intro
            "child" -> R.string.st_child_intro
            "seer" -> R.string.st_seer_intro
            "poet" -> R.string.st_poet_intro
            "inspector" -> R.string.st_inspector_intro
            "granny" -> R.string.st_granny_intro
            "traveller" -> R.string.st_traveller_intro
            else -> R.string.st_monk_intro
        }
    )

    /** A whistle-stop's name, made from [seed]: "Little Elms", «Малые Вязы». */
    fun halt(c: Context, seed: Int): String {
        val adj = c.resources.getStringArray(R.array.halt_adj)
        val noun = c.resources.getStringArray(R.array.halt_noun)
        return c.getString(R.string.halt_name, adj[seed % adj.size], noun[(seed / adj.size) % noun.size])
    }

    /** Carriage and seat numbers for a trip's ticket, from its seed. */
    fun carriage(seed: Long): Int = (Math.floorMod(seed, 12L) + 1).toInt()
    fun seats(seed: Long): Pair<Int, Int> {
        val s = (Math.floorMod(seed / 12, 9L) * 4 + 1).toInt()
        return s to s + 1
    }

    fun quote(c: Context, text: String): String = c.getString(R.string.quoted, text)
}

@Composable
fun ctx(): Context = LocalContext.current
