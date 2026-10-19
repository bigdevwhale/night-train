package app.nighttrain.ui.station

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.nighttrain.R
import app.nighttrain.data.Stage
import app.nighttrain.domain.Journal
import app.nighttrain.domain.Plan
import app.nighttrain.domain.Rails
import app.nighttrain.ui.Depot
import app.nighttrain.ui.Go
import app.nighttrain.ui.T
import app.nighttrain.ui.art.BiomeTile
import app.nighttrain.ui.art.TrainWindow
import app.nighttrain.ui.components.Bar
import app.nighttrain.ui.components.IconDisc
import app.nighttrain.ui.components.PrimaryButton
import app.nighttrain.ui.components.SectionTitle
import app.nighttrain.ui.components.Stat
import app.nighttrain.ui.components.UCard
import app.nighttrain.ui.ctx
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType

/** The platform you're standing on: where the train is, what's waiting, where it can go next. */
@Composable
fun StationTab(d: Depot, go: Go, openMap: () -> Unit) {
    val c = ctx()
    val city = d.city
    val last = d.finished.maxByOrNull { it.endedAt }
    val biome = last?.let { Rails.biome(it.from, it.to) } ?: Rails.departures(city).first().biome

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.st_here).uppercase(), style = UType.Eyebrow)
                Text(T.city(c, city), style = UType.Display)
            }
            IconButton(go::settings) { Icon(Icons.Rounded.Settings, stringResource(R.string.settings), tint = U.c.muted) }
        }
        Spacer(Modifier.height(12.dp))
        TrainWindow(biome, Stage.EVENING, Modifier.fillMaxWidth().aspectRatio(1.3f), station = T.city(c, city), seed = city.hashCode())
        Spacer(Modifier.height(14.dp))

        val ongoing = d.ongoing
        if (ongoing != null) {
            val stops = Plan.decode(ongoing.plan)
            UCard(onClick = { go.trip(ongoing.id) }) {
                Text(stringResource(R.string.st_ongoing).uppercase(), style = UType.Eyebrow.copy(color = U.c.brass))
                Spacer(Modifier.height(4.dp))
                Text("${T.city(c, ongoing.from)} → ${T.city(c, ongoing.to)}", style = UType.Title)
                Spacer(Modifier.height(10.dp))
                Bar(ongoing.pos / stops.size.coerceAtLeast(1).toFloat())
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.st_stop_of, ongoing.pos + 1, stops.size), style = UType.Small)
            }
            Spacer(Modifier.height(10.dp))
            PrimaryButton(stringResource(R.string.st_back_in), { go.trip(ongoing.id) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Train)
        } else {
            PrimaryButton(stringResource(R.string.st_buy), { go.board() }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Train)
        }

        if (d.waiting.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            UCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconDisc(Icons.Rounded.Mail, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(T.plural(c, R.plurals.notes_waiting, d.waiting.size), style = UType.Strong)
                        Text(stringResource(R.string.st_notes_sub, d.waiting.joinToString(", ") { T.passenger(c, d.settings, 1 - it.from) }), style = UType.Muted)
                    }
                }
            }
        }

        SectionTitle(stringResource(R.string.st_from_here))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Rails.departures(city).forEach { line ->
                val to = line.other(city)
                Row(
                    Modifier.fillMaxWidth().clip(U.RMd).background(U.c.surface).border(1.dp, U.c.line, U.RMd)
                        .clickable(enabled = ongoing == null) { go.board(to) }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BiomeTile(line.biome, Modifier.size(54.dp).clip(U.RSm), seed = to.hashCode())
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(T.city(c, to), style = UType.Name)
                            if (to !in d.visited) {
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.st_new).uppercase(), style = UType.Small.copy(color = U.c.brass))
                            }
                        }
                        Text("${T.biome(c, line.biome)} · ${T.themes(c, line.biome)}", style = UType.Muted, maxLines = 2)
                    }
                    if (ongoing == null) Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = U.c.faint)
                }
            }
        }

        SectionTitle(stringResource(R.string.st_log))
        val acc = Journal.accuracy(d.moments)
        val (asked, all) = Journal.coverage(d.deck, d.moments, d.settings.sleeper)
        UCard(onClick = openMap) {
            Row {
                Stat(stringResource(R.string.stat_trips), d.finished.size.toString(), Modifier.weight(1f))
                Stat(stringResource(R.string.stat_cities), "${d.visited.size}/${Rails.cities.size}", Modifier.weight(1f))
                Stat(stringResource(R.string.stat_guessed), acc?.let { "${(it * 100).toInt()}%" } ?: "—", Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.stat_asked, asked, all), style = UType.Muted)
            Spacer(Modifier.height(6.dp))
            Bar(asked / all.coerceAtLeast(1).toFloat())
        }
        Spacer(Modifier.height(24.dp))
    }
}
