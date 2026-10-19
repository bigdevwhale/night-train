package app.nighttrain.domain

/**
 * What the line runs through. It sets the view from the window and tilts the conductor toward a few
 * subjects: a coastline brings out dreams, a pine forest brings back childhood.
 */
enum class Biome(val themes: List<String>) {
    FOREST(listOf("childhood", "past", "family")),
    SEA(listOf("dreams", "future", "imagination")),
    LAKES(listOf("senses", "fears", "values")),
    MOUNTAINS(listOf("fears", "values", "work")),
    STEPPE(listOf("values", "future", "past")),
    SNOW(listOf("family", "us", "past")),
    CITY(listOf("work", "imagination", "quirks")),
    FIELDS(listOf("senses", "us", "dreams")),
}

/** A city on the map, at [x], [y] in 0..1 of the map's width and height. Its name comes from strings by [key]. */
data class City(val key: String, val x: Float, val y: Float)

data class Line(val a: String, val b: String, val biome: Biome) {
    fun other(city: String): String = if (city == a) b else a
    fun touches(city: String): Boolean = city == a || city == b
}

/** The railway of an imaginary country, with Midnight Junction in the middle. */
object Rails {
    const val HOME = "midnight"

    val cities = listOf(
        City(HOME, 0.50f, 0.50f),
        City("pines", 0.33f, 0.31f),
        City("owls", 0.11f, 0.20f),
        City("snow", 0.30f, 0.07f),
        City("aurora", 0.60f, 0.05f),
        City("mirror", 0.68f, 0.30f),
        City("pass", 0.87f, 0.17f),
        City("cinder", 0.88f, 0.42f),
        City("dawn", 0.92f, 0.64f),
        City("wormwood", 0.72f, 0.62f),
        City("starfall", 0.84f, 0.86f),
        City("vines", 0.55f, 0.80f),
        City("lights", 0.30f, 0.75f),
        City("tide", 0.10f, 0.90f),
        City("harbour", 0.10f, 0.58f),
        City("bells", 0.24f, 0.46f),
    )

    val lines = listOf(
        Line(HOME, "pines", Biome.FOREST),
        Line(HOME, "mirror", Biome.LAKES),
        Line(HOME, "wormwood", Biome.STEPPE),
        Line(HOME, "bells", Biome.FIELDS),
        Line(HOME, "lights", Biome.CITY),
        Line(HOME, "vines", Biome.FIELDS),
        Line("bells", "harbour", Biome.SEA),
        Line("harbour", "tide", Biome.SEA),
        Line("harbour", "lights", Biome.CITY),
        Line("lights", "tide", Biome.SEA),
        Line("lights", "vines", Biome.CITY),
        Line("pines", "owls", Biome.FOREST),
        Line("pines", "snow", Biome.SNOW),
        Line("owls", "snow", Biome.SNOW),
        Line("owls", "bells", Biome.FOREST),
        Line("snow", "aurora", Biome.SNOW),
        Line("mirror", "aurora", Biome.SNOW),
        Line("mirror", "pass", Biome.MOUNTAINS),
        Line("pass", "aurora", Biome.MOUNTAINS),
        Line("pass", "cinder", Biome.MOUNTAINS),
        Line("mirror", "cinder", Biome.LAKES),
        Line("cinder", "dawn", Biome.SEA),
        Line("wormwood", "dawn", Biome.STEPPE),
        Line("wormwood", "starfall", Biome.STEPPE),
        Line("vines", "starfall", Biome.STEPPE),
        Line("dawn", "starfall", Biome.SEA),
        Line("pines", "mirror", Biome.LAKES),
    )

    private val byKey = cities.associateBy { it.key }
    fun city(key: String): City = byKey[key] ?: byKey.getValue(HOME)

    /** The lines out of [city], each with where it goes. */
    fun departures(city: String): List<Line> = lines.filter { it.touches(city) }

    fun line(a: String, b: String): Line? = lines.firstOrNull { it.touches(a) && it.touches(b) && a != b }

    /** The line two cities are joined by, with "forest" when the route is unknown (an old backup, a removed line). */
    fun biome(a: String, b: String): Biome = line(a, b)?.biome ?: Biome.FOREST

    fun key(a: String, b: String): String = listOf(a, b).sorted().joinToString("-")
}
