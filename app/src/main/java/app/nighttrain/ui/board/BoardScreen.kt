package app.nighttrain.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.nighttrain.R
import app.nighttrain.container
import app.nighttrain.data.Length
import app.nighttrain.data.Trip
import app.nighttrain.domain.Director
import app.nighttrain.domain.Plan
import app.nighttrain.domain.Rails
import app.nighttrain.ui.Depot
import app.nighttrain.ui.Go
import app.nighttrain.ui.T
import app.nighttrain.ui.art.BiomeTile
import app.nighttrain.ui.art.Ticket
import app.nighttrain.ui.components.PrimaryButton
import app.nighttrain.ui.components.SectionTitle
import app.nighttrain.ui.components.TopBar
import app.nighttrain.ui.ctx
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/** The ticket office: where to, which train, and the ticket itself before you board. */
@Composable
fun BoardScreen(d: Depot, go: Go, preset: String) {
    val c = ctx()
    val container = c.container
    val scope = rememberCoroutineScope()
    val city = d.city
    val lines = Rails.departures(city)
    var to by rememberSaveable { mutableStateOf(preset.takeIf { p -> lines.any { it.other(city) == p } } ?: lines.first().other(city)) }
    var length by rememberSaveable { mutableStateOf(Length.EXPRESS) }
    var sleeper by rememberSaveable { mutableStateOf(false) }
    val seed = rememberSaveable { Random.nextLong() }
    var boarding by remember { mutableStateOf(false) }
    val line = Rails.line(city, to) ?: lines.first()

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        TopBar(stringResource(R.string.board_title), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Ticket(
                from = T.city(c, city), to = T.city(c, to), seed = seed, date = System.currentTimeMillis(),
                train = T.length(c, length), holes = 0, modifier = Modifier.fillMaxWidth(),
            )

            SectionTitle(stringResource(R.string.board_where))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lines.forEach { l ->
                    val dest = l.other(city)
                    val on = dest == to
                    Row(
                        Modifier.fillMaxWidth().clip(U.RMd)
                            .background(if (on) U.c.brassSoft else U.c.surface)
                            .border(1.dp, if (on) U.c.brass.copy(alpha = 0.6f) else U.c.line, U.RMd)
                            .clickable { to = dest }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BiomeTile(l.biome, Modifier.size(54.dp).clip(U.RSm), seed = dest.hashCode())
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(T.city(c, dest), style = UType.Name.copy(color = if (on) U.c.brass else U.c.ink))
                            Text(T.biome(c, l.biome), style = UType.Muted)
                            Text(T.themes(c, l.biome), style = UType.Small, maxLines = 1)
                        }
                        if (dest !in d.visited) Text(stringResource(R.string.st_new).uppercase(), style = UType.Small.copy(color = U.c.brass))
                    }
                }
            }

            SectionTitle(stringResource(R.string.board_train))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Length.entries.forEach { l ->
                    val on = l == length
                    Column(
                        Modifier.weight(1f).clip(U.RMd)
                            .background(if (on) U.c.brassSoft else U.c.surface)
                            .border(1.dp, if (on) U.c.brass.copy(alpha = 0.6f) else U.c.line, U.RMd)
                            .clickable { length = l }.padding(horizontal = 10.dp, vertical = 12.dp),
                    ) {
                        Text(T.length(c, l), style = UType.Strong.copy(color = if (on) U.c.brass else U.c.ink), maxLines = 2)
                        Spacer(Modifier.height(4.dp))
                        Text(T.lengthSub(c, l), style = UType.Small, maxLines = 2)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.board_line_note, T.biome(c, line.biome), T.themes(c, line.biome)), style = UType.Muted)

            if (d.settings.sleeper) {
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth().clip(U.RMd).background(U.c.surface).border(1.dp, U.c.line, U.RMd).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.board_sleeper), style = UType.Strong)
                        Text(stringResource(R.string.board_sleeper_sub), style = UType.Muted)
                    }
                    Switch(sleeper, { sleeper = it }, colors = SwitchDefaults.colors(checkedTrackColor = U.c.velvet, checkedThumbColor = U.c.paper))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        PrimaryButton(
            stringResource(R.string.board_go),
            {
                if (boarding) return@PrimaryButton
                boarding = true
                scope.launch {
                    val id = withContext(Dispatchers.Default) {
                        val stops = Director.plan(
                            Director.Input(
                                deck = d.deck, length = length, biome = line.biome, seed = seed,
                                history = d.moments, sleeper = sleeper && d.settings.sleeper, notes = d.notes,
                            )
                        )
                        container.db.dao().insert(
                            Trip(from = city, to = to, seed = seed, length = length.name, plan = Plan.encode(stops), startedAt = System.currentTimeMillis())
                        )
                    }
                    go.trip(id)
                }
            },
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 14.dp).navigationBarsPadding(),
            enabled = d.ongoing == null && !boarding,
            icon = Icons.Rounded.Train,
        )
    }
}
