package app.nighttrain.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nighttrain.R
import app.nighttrain.domain.Biome
import app.nighttrain.domain.Rails
import app.nighttrain.ui.Depot
import app.nighttrain.ui.Go
import app.nighttrain.ui.T
import app.nighttrain.ui.art.BiomeTile
import app.nighttrain.ui.components.PrimaryButton
import app.nighttrain.ui.components.SectionTitle
import app.nighttrain.ui.components.UCard
import app.nighttrain.ui.ctx
import app.nighttrain.ui.theme.DisplayFont
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType
import kotlin.math.hypot

/** Each landscape's colour on the map. */
fun biomeColor(b: Biome): Color = when (b) {
    Biome.FOREST -> Color(0xFF6FA77A)
    Biome.SEA -> Color(0xFF5F9ED8)
    Biome.LAKES -> Color(0xFF63C1BA)
    Biome.MOUNTAINS -> Color(0xFFA996C9)
    Biome.STEPPE -> Color(0xFFD7A257)
    Biome.SNOW -> Color(0xFFD5E1F0)
    Biome.CITY -> Color(0xFFF2C46D)
    Biome.FIELDS -> Color(0xFFB9C66A)
}

/** The railway as drawn on the map in the corridor: lines by landscape, the cities you've reached, the train where it stands. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapTab(d: Depot, go: Go) {
    val c = ctx()
    var picked by rememberSaveable { mutableStateOf(d.city) }
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.map_eyebrow).uppercase(), style = UType.Eyebrow)
        Text(stringResource(R.string.map_title), style = UType.Display)
        Text(stringResource(R.string.map_sub, d.visited.size, Rails.cities.size, d.travelled.size, Rails.lines.size), style = UType.Muted)
        Spacer(Modifier.height(12.dp))
        RailMap(d, picked, Modifier.fillMaxWidth().aspectRatio(0.82f)) { picked = it }

        val line = Rails.line(d.city, picked)
        SectionTitle(if (picked == d.city) stringResource(R.string.map_here) else stringResource(R.string.map_city))
        UCard {
            Text(T.city(c, picked), style = UType.Title)
            Spacer(Modifier.height(4.dp))
            val visits = d.finished.count { it.to == picked }
            Text(
                when {
                    picked == d.city -> stringResource(R.string.map_train_here)
                    picked in d.visited -> T.plural(c, R.plurals.arrivals, visits)
                    else -> stringResource(R.string.map_never)
                },
                style = UType.Muted,
            )
            Spacer(Modifier.height(10.dp))
            Rails.departures(picked).forEach { l ->
                Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(biomeColor(l.biome)))
                    Spacer(Modifier.width(8.dp))
                    Text("${T.city(c, l.other(picked))} · ${T.biome(c, l.biome)}", style = UType.Body.copy(fontSize = 14.sp))
                }
            }
            if (line != null && d.ongoing == null) {
                Spacer(Modifier.height(12.dp))
                PrimaryButton(stringResource(R.string.map_go, T.city(c, picked)), { go.board(picked) }, Modifier.fillMaxWidth())
            } else if (picked != d.city) {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.map_far), style = UType.Small)
            }
        }

        SectionTitle(stringResource(R.string.map_legend))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Biome.entries.forEach { b ->
                Row(
                    Modifier.clip(U.RSm).background(U.c.surface).border(1.dp, U.c.line, U.RSm).padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BiomeTile(b, Modifier.size(34.dp).clip(U.RSm), seed = b.ordinal)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(T.biome(c, b), style = UType.Strong.copy(fontSize = 13.sp, color = biomeColor(b)))
                        Text(T.themes(c, b), style = UType.Small.copy(fontSize = 10.sp))
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun RailMap(d: Depot, picked: String?, modifier: Modifier = Modifier, onPick: (String) -> Unit = {}) {
    val c = ctx()
    val measurer = rememberTextMeasurer()
    val paper = U.c.surface
    val ink = U.c.ink
    val faint = U.c.faint
    val brass = U.c.brass
    val bg = U.c.bg
    val names = Rails.cities.associate { it.key to T.city(c, it.key) }
    Canvas(
        modifier.clip(U.RLg).background(paper).border(1.dp, U.c.line, U.RLg)
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    val pad = 28.dp.toPx()
                    val w = size.width - pad * 2
                    val h = size.height - pad * 2
                    Rails.cities.minByOrNull { hypot(pad + it.x * w - p.x, pad + it.y * h - p.y) }
                        ?.takeIf { hypot(pad + it.x * w - p.x, pad + it.y * h - p.y) < 40.dp.toPx() }
                        ?.let { onPick(it.key) }
                }
            },
    ) {
        val pad = 28.dp.toPx()
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        fun at(key: String) = Rails.city(key).let { Offset(pad + it.x * w, pad + it.y * h) }
        // A faint grid like a surveyor's sheet.
        for (i in 1..7) {
            drawLine(faint.copy(alpha = 0.08f), Offset(size.width * i / 8f, 0f), Offset(size.width * i / 8f, size.height), 1f)
            drawLine(faint.copy(alpha = 0.08f), Offset(0f, size.height * i / 8f), Offset(size.width, size.height * i / 8f), 1f)
        }
        Rails.lines.forEach { l ->
            val travelled = (d.travelled[Rails.key(l.a, l.b)] ?: 0) > 0
            val col = biomeColor(l.biome)
            val a = at(l.a)
            val b = at(l.b)
            if (travelled) {
                drawLine(col, a, b, 5.dp.toPx(), cap = StrokeCap.Round)
                // Sleepers across the travelled rails.
                drawLine(bg, a, b, 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 7f)))
            } else {
                drawLine(col.copy(alpha = 0.45f), a, b, 2.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
            }
        }
        Rails.cities.forEach { city ->
            val p = at(city.key)
            val seen = city.key in d.visited
            val here = city.key == d.city
            val sel = city.key == picked
            if (sel) drawCircle(brass.copy(alpha = 0.25f), 16.dp.toPx(), p)
            drawCircle(if (seen) ink else bg, 6.dp.toPx(), p)
            drawCircle(if (seen) brass else faint, 6.dp.toPx(), p, style = Stroke(2.dp.toPx()))
            if (here) {
                // The locomotive: a brass lozenge with a lamp.
                drawRoundRect(brass, p + Offset(-11.dp.toPx(), -24.dp.toPx()), Size(22.dp.toPx(), 12.dp.toPx()), CornerRadius(3.dp.toPx()))
                drawCircle(Color(0xFFFFF0C2), 2.5.dp.toPx(), p + Offset(8.dp.toPx(), -18.dp.toPx()))
            }
            val label = measurer.measure(names.getValue(city.key), TextStyle(fontFamily = DisplayFont, fontSize = 11.sp, color = if (seen) ink else faint))
            val lx = (p.x - label.size.width / 2f).coerceIn(4f, size.width - label.size.width - 4f)
            val ly = p.y + 9.dp.toPx()
            drawText(label, topLeft = Offset(lx, ly))
        }
    }
}
