package app.nighttrain.ui.art

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import app.nighttrain.SHOTS
import app.nighttrain.data.Stage
import app.nighttrain.domain.Biome
import app.nighttrain.ui.theme.DisplayFont
import kotlin.math.floor
import kotlin.math.sin

/** A steady pseudo-random 0..1 for an integer position: the same tree stands in the same place every pass. */
private fun hash(i: Int, salt: Int): Float {
    var x = i * 374761393 + salt * 668265263
    x = (x xor (x ushr 13)) * 1274126177
    x = x xor (x ushr 16)
    return (x and 0xFFFFFF) / 16777215f
}

/** Smooth noise along x, for hill lines. */
private fun ridge(x: Float, salt: Int, scale: Float): Float {
    val p = x / scale
    val i = floor(p).toInt()
    val f = p - i
    val s = f * f * (3 - 2 * f)
    return hash(i, salt) * (1 - s) + hash(i + 1, salt) * s
}

/** The sky and its silhouettes, by the hour of the trip. */
private class Light(val top: Color, val mid: Color, val low: Color, val far: Color, val mid2: Color, val near: Color, val stars: Float, val glow: Color)

private fun light(stage: Stage): Light = when (stage) {
    Stage.EVENING, Stage.ANY -> Light(
        Color(0xFF1C2550), Color(0xFF5E4677), Color(0xFFE48A5C),
        Color(0xFF4B3A62), Color(0xFF2B2343), Color(0xFF15111F), 0.25f, Color(0xFFFFB27A),
    )
    Stage.NIGHT -> Light(
        Color(0xFF050914), Color(0xFF0E1630), Color(0xFF23345A),
        Color(0xFF1A2440), Color(0xFF0F1629), Color(0xFF070A13), 1f, Color(0xFFE9E4CF),
    )
    Stage.TUNNEL -> Light(
        Color(0xFF060607), Color(0xFF0B0B0E), Color(0xFF111116),
        Color(0xFF0B0B0E), Color(0xFF09090B), Color(0xFF050506), 0f, Color(0xFFFFB45E),
    )
    Stage.DAWN -> Light(
        Color(0xFF26295A), Color(0xFF8A6B9E), Color(0xFFF4B48E),
        Color(0xFF6A5A86), Color(0xFF3F3560), Color(0xFF1D1830), 0.15f, Color(0xFFFFD6A0),
    )
}

/**
 * The view running past: the time on the rails in seconds, scaled by how fast the train goes. Holds
 * still in the screenshot build.
 */
@Composable
fun rememberTravel(moving: Boolean): State<Float> {
    val pos = remember { mutableFloatStateOf(if (SHOTS) 37f else 0f) }
    val speed by animateFloatAsState(if (moving) 1f else 0f, tween(2600), label = "speed")
    val sp = rememberUpdatedState(speed)
    // Frames are only asked for while the train moves or is still slowing down: a standing view costs nothing.
    LaunchedEffect(moving) {
        if (SHOTS) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (moving || sp.value > 0.001f) {
            withFrameNanos { n ->
                pos.floatValue += (n - last) / 1e9f * sp.value
                last = n
            }
        }
    }
    return pos
}

/** Seconds since shown, for things that move whether or not the train does (steam, lamps). */
@Composable
fun rememberClock(running: Boolean = true): State<Float> {
    val t = remember { mutableFloatStateOf(if (SHOTS) 4f else 0f) }
    LaunchedEffect(running) {
        if (SHOTS || !running) return@LaunchedEffect
        val start = withFrameNanos { it }
        while (true) withFrameNanos { n -> t.floatValue = (n - start) / 1e9f }
    }
    return t
}

/**
 * The compartment window: the [biome]'s landscape at the [stage]'s hour, running past at speed, inside
 * a mahogany frame with a velvet valance and a glass of tea on the table. [station] stands the train at a
 * platform with its name on the sign. [frame] off gives the bare view, for backdrops.
 */
@Composable
fun TrainWindow(
    biome: Biome,
    stage: Stage,
    modifier: Modifier = Modifier,
    moving: Boolean = true,
    station: String? = null,
    frame: Boolean = true,
    tea: Boolean = true,
    seed: Int = 0,
) {
    val travel = rememberTravel(moving && station == null)
    val clock = rememberClock(frame && tea)
    val measurer = rememberTextMeasurer()
    val lit = remember(stage) { light(stage) }
    Canvas(modifier) {
        val x = travel.value
        val t = clock.value
        val pad = if (frame) size.minDimension * 0.06f else 0f
        val inner = Size(size.width - pad * 2, size.height - pad * 2 - if (frame) pad * 0.6f else 0f)
        val corner = CornerRadius(if (frame) pad * 1.3f else 0f)
        val glass = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(pad, pad, pad + inner.width, pad + inner.height, corner)) }
        clipPath(glass) {
            translate(pad, pad) {
                scenery(inner, biome, stage, lit, x, t, seed, station, measurer)
            }
            // The glass: a faint pane reflection.
            drawRect(
                Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.05f), Color.Transparent),
                    start = Offset(size.width * 0.15f, 0f), end = Offset(size.width * 0.55f, size.height),
                ),
            )
        }
        if (frame) frame(pad, inner, corner, clipOut = glass)
        if (frame && tea) tea(pad, inner, t)
    }
}

private fun DrawScope.scenery(s: Size, biome: Biome, stage: Stage, lit: Light, x: Float, t: Float, seed: Int, station: String?, measurer: TextMeasurer) {
    val w = s.width
    val h = s.height
    val horizon = h * 0.62f
    drawRect(Brush.verticalGradient(0f to lit.top, 0.55f to lit.mid, 1f to lit.low, endY = horizon), size = Size(w, horizon + 2))

    if (stage == Stage.TUNNEL) {
        tunnel(s, x, lit)
        return
    }
    // Stars and the moon (or the sun low on the line).
    if (lit.stars > 0f) {
        repeat(70) { i ->
            val sx = hash(i, 11 + seed) * w
            val sy = hash(i, 12 + seed) * horizon * 0.8f
            val tw = 0.55f + 0.45f * sin(i * 1.7f + x * 0.4f)
            drawCircle(Color.White.copy(alpha = lit.stars * tw * (0.25f + hash(i, 13) * 0.6f) * (1f - sy / horizon)), radius = 0.6f + hash(i, 14) * 1.3f, center = Offset(sx, sy))
        }
    }
    val orb = when (stage) {
        Stage.NIGHT -> Offset(w * 0.78f, h * 0.17f)
        Stage.DAWN -> Offset(w * 0.3f, horizon * 0.95f)
        else -> Offset(w * 0.68f, horizon * 0.9f)
    }
    val orbR = if (stage == Stage.NIGHT) h * 0.055f else h * 0.09f
    drawCircle(Brush.radialGradient(listOf(lit.glow.copy(alpha = 0.35f), Color.Transparent), orb, orbR * 4), orbR * 4, orb)
    drawCircle(lit.glow, orbR, orb)
    if (stage == Stage.NIGHT) drawCircle(lit.top.copy(alpha = 0.9f), orbR * 0.86f, orb + Offset(orbR * 0.42f, -orbR * 0.18f))

    far(s, biome, lit, x * 6f, horizon, seed, orb)
    mid(s, biome, lit, x * 40f, horizon, seed)
    ground(s, lit, horizon)
    if (station != null) platform(s, lit, station, measurer) else poles(s, lit, x * 170f)
    if (biome == Biome.SNOW) snowfall(s, x, seed)
}

/** Mountains, hills, sea or a skyline: the slowest layer. */
private fun DrawScope.far(s: Size, biome: Biome, lit: Light, off: Float, horizon: Float, seed: Int, orb: Offset) {
    val w = s.width
    val h = s.height
    when (biome) {
        Biome.SEA -> {
            val sea = horizon - h * 0.1f
            drawRect(Brush.verticalGradient(listOf(lerp(lit.far, lit.low, 0.25f), lit.near), sea, h), Offset(0f, sea), Size(w, h - sea))
            // The light's road on the water.
            repeat(14) { i ->
                val y = sea + (i + 1) * (horizon - sea + h * 0.06f) / 14f
                val half = w * (0.015f + i * 0.007f) * (0.6f + 0.4f * sin(i * 2.1f + off * 0.05f))
                drawLine(lit.glow.copy(alpha = 0.5f - i * 0.025f), Offset(orb.x - half, y), Offset(orb.x + half, y), strokeWidth = 1.6f)
            }
        }
        Biome.LAKES -> {
            hills(s, lerp(lit.far, lit.mid, 0.2f), horizon - h * 0.16f, h * 0.1f, off * 0.6f, 31 + seed, 140f)
            val lake = horizon - h * 0.06f
            drawRect(Brush.verticalGradient(listOf(lerp(lit.low, lit.far, 0.4f), lit.far), lake, horizon), Offset(0f, lake), Size(w, horizon - lake + 4))
            drawLine(lit.glow.copy(alpha = 0.4f), Offset(orb.x - w * 0.05f, lake + 4), Offset(orb.x + w * 0.05f, lake + 4), 1.5f)
        }
        Biome.MOUNTAINS -> peaks(s, lit, horizon, off, seed)
        Biome.SNOW -> {
            hills(s, lerp(lit.far, Color(0xFFDCE4F0), 0.35f), horizon - h * 0.12f, h * 0.12f, off, 41 + seed, 160f)
        }
        Biome.CITY -> skyline(s, lit, horizon, off, seed)
        Biome.FOREST -> hills(s, lit.far, horizon - h * 0.13f, h * 0.12f, off, 51 + seed, 120f)
        Biome.FIELDS -> hills(s, lit.far, horizon - h * 0.06f, h * 0.07f, off, 61 + seed, 220f)
        Biome.STEPPE -> hills(s, lit.far, horizon - h * 0.025f, h * 0.025f, off, 71 + seed, 300f)
    }
}

private fun DrawScope.hills(s: Size, color: Color, base: Float, amp: Float, off: Float, salt: Int, scale: Float) {
    val p = Path()
    p.moveTo(0f, s.height)
    var xx = 0f
    while (xx <= s.width + 8f) {
        p.lineTo(xx, base - ridge(xx + off, salt, scale) * amp - ridge(xx + off, salt + 5, scale / 3) * amp * 0.25f)
        xx += 6f
    }
    p.lineTo(s.width, s.height)
    p.close()
    drawPath(p, color)
}

private fun DrawScope.peaks(s: Size, lit: Light, horizon: Float, off: Float, seed: Int) {
    val seg = s.width * 0.22f
    val first = floor(off / seg).toInt() - 1
    for (i in first..first + (s.width / seg).toInt() + 2) {
        val cx = i * seg - off + hash(i, 81 + seed) * seg * 0.5f
        val ph = s.height * (0.22f + hash(i, 82 + seed) * 0.22f)
        val half = seg * (0.55f + hash(i, 83) * 0.4f)
        val top = Offset(cx, horizon - ph)
        val body = Path().apply { moveTo(cx - half, horizon + 2); lineTo(top.x, top.y); lineTo(cx + half, horizon + 2); close() }
        drawPath(body, lit.far)
        // Snow on the top third.
        val cap = Path().apply {
            moveTo(top.x, top.y)
            lineTo(cx + half * 0.3f, top.y + ph * 0.3f)
            lineTo(cx + half * 0.08f, top.y + ph * 0.24f)
            lineTo(cx - half * 0.1f, top.y + ph * 0.33f)
            lineTo(cx - half * 0.3f, top.y + ph * 0.3f)
            close()
        }
        drawPath(cap, lerp(lit.far, Color(0xFFE8ECF4), 0.55f))
    }
}

private fun DrawScope.skyline(s: Size, lit: Light, horizon: Float, off: Float, seed: Int) {
    val seg = s.width * 0.07f
    val first = floor(off / seg).toInt() - 1
    for (i in first..first + (s.width / seg).toInt() + 2) {
        val bx = i * seg - off
        val bh = s.height * (0.08f + hash(i, 91 + seed) * 0.22f) * (if (hash(i, 92) > 0.92f) 1.6f else 1f)
        val bw = seg * (0.75f + hash(i, 93) * 0.3f)
        drawRect(lit.far, Offset(bx, horizon - bh), Size(bw, bh + 2))
        // Windows lit at random, a few warm.
        val rows = (bh / 9f).toInt()
        for (r in 1 until rows) for (c in 0..2) {
            if (hash(i * 31 + r * 7 + c, 94 + seed) > 0.72f) {
                drawRect(Color(0xFFF6CB78).copy(alpha = 0.75f), Offset(bx + bw * (0.18f + c * 0.26f), horizon - bh + r * 9f), Size(bw * 0.12f, 3.5f))
            }
        }
    }
}

/** Trees and things at middle distance. */
private fun DrawScope.mid(s: Size, biome: Biome, lit: Light, off: Float, horizon: Float, seed: Int) {
    val h = s.height
    val seg = when (biome) {
        Biome.FOREST, Biome.SNOW -> 13f
        Biome.STEPPE -> 120f
        Biome.SEA -> 160f
        else -> 34f
    }
    val first = floor(off / seg).toInt() - 2
    for (i in first..first + (s.width / seg).toInt() + 4) {
        val r = hash(i, 101 + seed)
        val bx = i * seg - off + r * seg * 0.6f
        when (biome) {
            Biome.FOREST, Biome.SNOW, Biome.MOUNTAINS, Biome.LAKES -> {
                if (biome != Biome.FOREST && biome != Biome.SNOW && r < 0.45f) continue
                val th = h * (0.1f + hash(i, 102) * 0.1f)
                pine(Offset(bx, horizon + h * 0.02f), th, lit.mid2, if (biome == Biome.SNOW) Color(0xFFDDE5F2).copy(alpha = 0.7f) else null)
            }
            Biome.FIELDS -> {
                if (r < 0.5f) {
                    val tr = h * (0.025f + hash(i, 103) * 0.03f)
                    drawLine(lit.mid2, Offset(bx, horizon + h * 0.02f), Offset(bx, horizon - tr), strokeWidth = 2.5f)
                    drawCircle(lit.mid2, tr, Offset(bx, horizon - tr * 1.4f))
                } else if (r > 0.85f) {
                    drawArc(lit.mid2, 180f, 180f, true, Offset(bx, horizon - h * 0.02f), Size(h * 0.05f, h * 0.05f))
                }
            }
            Biome.STEPPE -> {
                val tr = h * 0.03f
                if (r > 0.55f) {
                    drawLine(lit.mid2, Offset(bx, horizon + 2), Offset(bx, horizon - tr * 1.6f), strokeWidth = 2f)
                    drawOval(lit.mid2, Offset(bx - tr * 1.3f, horizon - tr * 2.4f), Size(tr * 2.6f, tr))
                }
            }
            Biome.SEA -> if (r > 0.6f) {
                // A rock, sometimes a lighthouse on it.
                val rw = h * 0.08f
                drawOval(lit.mid2, Offset(bx, horizon - h * 0.03f), Size(rw, h * 0.06f))
                if (r > 0.88f) {
                    drawRect(lit.mid2, Offset(bx + rw * 0.4f, horizon - h * 0.13f), Size(rw * 0.16f, h * 0.11f))
                    drawCircle(Color(0xFFF6CB78), 2.6f, Offset(bx + rw * 0.48f, horizon - h * 0.135f))
                }
            }
            Biome.CITY -> if (r > 0.4f) {
                val bh = h * (0.05f + hash(i, 104) * 0.08f)
                drawRect(lit.mid2, Offset(bx, horizon - bh), Size(seg * 0.8f, bh + 4))
                if (hash(i, 105) > 0.5f) drawRect(Color(0xFFF6CB78).copy(alpha = 0.6f), Offset(bx + seg * 0.2f, horizon - bh * 0.7f), Size(seg * 0.12f, 4f))
            }
        }
    }
}

private fun DrawScope.pine(base: Offset, height: Float, color: Color, snow: Color?) {
    val w = height * 0.42f
    val p = Path().apply {
        moveTo(base.x, base.y - height)
        lineTo(base.x + w * 0.35f, base.y - height * 0.62f)
        lineTo(base.x + w * 0.22f, base.y - height * 0.62f)
        lineTo(base.x + w * 0.5f, base.y - height * 0.22f)
        lineTo(base.x - w * 0.5f, base.y - height * 0.22f)
        lineTo(base.x - w * 0.22f, base.y - height * 0.62f)
        lineTo(base.x - w * 0.35f, base.y - height * 0.62f)
        close()
    }
    drawPath(p, color)
    drawRect(color, Offset(base.x - 1.2f, base.y - height * 0.25f), Size(2.4f, height * 0.25f))
    if (snow != null) {
        drawPath(Path().apply { moveTo(base.x, base.y - height); lineTo(base.x + w * 0.16f, base.y - height * 0.8f); lineTo(base.x - w * 0.16f, base.y - height * 0.8f); close() }, snow)
    }
}

private fun DrawScope.ground(s: Size, lit: Light, horizon: Float) {
    val top = horizon + s.height * 0.02f
    drawRect(Brush.verticalGradient(listOf(lit.mid2, lit.near), top, s.height), Offset(0f, top), Size(s.width, s.height - top))
}

/** Telegraph poles and their sagging wires, the fastest thing in the window. */
private fun DrawScope.poles(s: Size, lit: Light, off: Float) {
    val gap = s.width * 0.75f
    val first = floor(off / gap).toInt() - 1
    val topY = s.height * 0.3f
    val color = lit.near
    for (i in first..first + 3) {
        val px = i * gap - off
        drawLine(color, Offset(px, s.height), Offset(px, topY), strokeWidth = 5f)
        drawLine(color, Offset(px - 12f, topY + 6f), Offset(px + 12f, topY + 6f), strokeWidth = 3f)
        // Wires to the next pole.
        for (k in 0..1) {
            val y0 = topY + 6f + k * 9f
            val p = Path().apply {
                moveTo(px + (k * 2 - 1) * 10f, y0)
                quadraticTo(px + gap / 2, y0 + s.height * 0.07f, px + gap + (k * 2 - 1) * 10f, y0)
            }
            drawPath(p, color.copy(alpha = 0.8f), style = Stroke(1.3f))
        }
    }
}

/** At a station: the platform edge, a lamp and the name board. */
private fun DrawScope.platform(s: Size, lit: Light, name: String, measurer: TextMeasurer) {
    val w = s.width
    val h = s.height
    drawRect(lerp(lit.near, Color(0xFF2A2633), 0.5f), Offset(0f, h * 0.84f), Size(w, h * 0.16f))
    drawLine(Color(0xFFD6AE62).copy(alpha = 0.6f), Offset(0f, h * 0.84f), Offset(w, h * 0.84f), 2f)
    // The lamp post, its glow.
    val lx = w * 0.18f
    drawCircle(Brush.radialGradient(listOf(Color(0x66F6CB78), Color.Transparent), Offset(lx, h * 0.3f), h * 0.35f), h * 0.35f, Offset(lx, h * 0.3f))
    drawLine(lit.near, Offset(lx, h * 0.86f), Offset(lx, h * 0.3f), 4f)
    drawCircle(Color(0xFFFFE2A6), 6f, Offset(lx, h * 0.3f))
    // The name board on two legs.
    val board = measurer.measure(name.uppercase(), TextStyle(fontFamily = DisplayFont, fontSize = (h / 16f).coerceIn(12f, 22f).sp, color = Color(0xFFF1E8D6), letterSpacing = 1.5.sp))
    val bw = board.size.width + 28f
    val bh = board.size.height + 14f
    val bx = w * 0.55f - bw / 2
    val by = h * 0.46f
    drawLine(lit.near, Offset(bx + 10f, by + bh), Offset(bx + 10f, h * 0.85f), 3f)
    drawLine(lit.near, Offset(bx + bw - 10f, by + bh), Offset(bx + bw - 10f, h * 0.85f), 3f)
    drawRoundRect(Color(0xFF1E2A44), Offset(bx, by), Size(bw, bh), CornerRadius(4f))
    drawRoundRect(Color(0xFFD6AE62), Offset(bx, by), Size(bw, bh), CornerRadius(4f), style = Stroke(2f))
    drawText(board, topLeft = Offset(bx + 14f, by + 7f))
}

/** Inside the tunnel: dark walls and a lamp sliding past now and then. */
private fun DrawScope.tunnel(s: Size, x: Float, lit: Light) {
    drawRect(lit.mid, size = s)
    val gap = s.width * 1.3f
    val off = x * 260f
    val first = floor(off / gap).toInt() - 1
    for (i in first..first + 2) {
        val lx = i * gap - off
        val c = Offset(lx, s.height * 0.35f)
        drawCircle(Brush.radialGradient(listOf(lit.glow.copy(alpha = 0.45f), Color.Transparent), c, s.height * 0.4f), s.height * 0.4f, c)
        drawCircle(lit.glow, 4f, c)
    }
    // Ribs of the tunnel wall streaking past.
    val rib = 46f
    val firstRib = floor(off / rib).toInt()
    for (i in firstRib..firstRib + (s.width / rib).toInt() + 1) {
        val rx = i * rib - off
        drawLine(Color.White.copy(alpha = 0.025f), Offset(rx, 0f), Offset(rx - 30f, s.height), 6f)
    }
}

private fun DrawScope.snowfall(s: Size, x: Float, seed: Int) {
    repeat(50) { i ->
        val sx = ((hash(i, 121 + seed) * s.width * 1.4f - x * 90f * (0.6f + hash(i, 122))) % (s.width * 1.4f) + s.width * 1.4f) % (s.width * 1.4f) - s.width * 0.2f
        val sy = (hash(i, 123) * s.height + x * 40f * (0.5f + hash(i, 124))) % s.height
        drawCircle(Color.White.copy(alpha = 0.55f), 1f + hash(i, 125) * 1.6f, Offset(sx, sy))
    }
}

/** Mahogany around the glass, a brass catch and a velvet valance with its fringe. */
private fun DrawScope.frame(pad: Float, inner: Size, corner: CornerRadius, clipOut: Path) {
    clipPath(clipOut, ClipOp.Difference) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF7A3E28), Color(0xFF5A2C1C), Color(0xFF3E1D13))))
        // Grain.
        repeat(18) { i ->
            val y = size.height * hash(i, 131)
            drawLine(Color.Black.copy(alpha = 0.08f), Offset(0f, y), Offset(size.width, y + 6f * sin(i.toFloat())), 1.2f)
        }
    }
    drawRoundRect(Color(0xFFD6AE62).copy(alpha = 0.7f), Offset(pad, pad), inner, corner, style = Stroke(2.2f))
    // The valance across the top.
    val vh = inner.height * 0.12f
    val folds = 9
    val fw = size.width / folds
    for (i in 0 until folds) {
        val p = Path().apply {
            moveTo(i * fw, 0f)
            lineTo((i + 1) * fw, 0f)
            lineTo((i + 1) * fw, pad + vh * 0.8f)
            quadraticTo((i + 0.5f) * fw, pad + vh * 1.25f, i * fw, pad + vh * 0.8f)
            close()
        }
        drawPath(p, Brush.horizontalGradient(listOf(Color(0xFF7E2A31), Color(0xFFA83C43), Color(0xFF6A2229)), i * fw, (i + 1) * fw))
        drawCircle(Color(0xFFD6AE62), 2.2f, Offset((i + 0.5f) * fw, pad + vh * 1.05f))
    }
    // The brass catch at the bottom of the pane.
    drawRoundRect(Color(0xFFD6AE62), Offset(size.width / 2 - 22f, pad + inner.height - 6f), Size(44f, 10f), CornerRadius(4f))
}

/** A glass of tea in a brass holder on the table, steam rising. */
private fun DrawScope.tea(pad: Float, inner: Size, t: Float) {
    val gh = inner.height * 0.26f
    val gw = gh * 0.56f
    val bx = size.width - pad - gw * 1.6f
    val by = size.height - pad * 0.35f
    // Steam.
    for (k in 0..2) {
        val phase = t * 0.6f + k * 2.1f
        val p = Path()
        val sx = bx + gw * (0.3f + k * 0.2f)
        p.moveTo(sx, by - gh)
        for (j in 1..10) {
            val yy = by - gh - j * gh * 0.09f
            p.lineTo(sx + sin(phase + j * 0.6f) * gw * 0.12f, yy)
        }
        drawPath(p, Color.White.copy(alpha = 0.10f), style = Stroke(2.2f, cap = StrokeCap.Round))
    }
    // Glass and tea.
    val glass = Path().apply {
        moveTo(bx, by - gh)
        lineTo(bx + gw, by - gh)
        lineTo(bx + gw * 0.92f, by)
        lineTo(bx + gw * 0.08f, by)
        close()
    }
    drawPath(glass, Color.White.copy(alpha = 0.16f))
    drawRect(Brush.verticalGradient(listOf(Color(0xFFB4542A), Color(0xFF6E2A12)), by - gh * 0.8f, by), Offset(bx + gw * 0.06f, by - gh * 0.8f), Size(gw * 0.88f, gh * 0.8f))
    drawLine(Color(0xFFD9D9D9), Offset(bx + gw * 0.7f, by - gh * 1.15f), Offset(bx + gw * 0.45f, by - gh * 0.3f), 2f)
    // The holder: brass lattice over the lower half, and the handle.
    val hy = by - gh * 0.55f
    drawRect(Brush.verticalGradient(listOf(Color(0xFFE9C77E), Color(0xFF9C7537)), hy, by), Offset(bx - 2f, hy), Size(gw + 4f, by - hy))
    for (k in 1..4) drawLine(Color(0xFF6E5024), Offset(bx + k * gw / 5f, hy + 3f), Offset(bx + k * gw / 5f, by - 3f), 1.4f)
    drawArc(Color(0xFFC9A25A), -90f, 180f, false, Offset(bx + gw - 2f, hy + 2f), Size(gw * 0.45f, (by - hy) * 0.8f), style = Stroke(3.2f))
    drawLine(Color(0xFFC9A25A), Offset(bx - 6f, by), Offset(bx + gw + 6f, by), 3f)
}

/** A small landscape tile for a line on the board or the map: the biome in a few strokes, at night. */
@Composable
fun BiomeTile(biome: Biome, modifier: Modifier = Modifier, stage: Stage = Stage.EVENING, seed: Int = 0) =
    TrainWindow(biome, stage, modifier, moving = false, frame = false, tea = false, seed = seed)
