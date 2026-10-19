package app.nighttrain.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nighttrain.R
import app.nighttrain.container
import app.nighttrain.data.Note
import app.nighttrain.data.Stage
import app.nighttrain.data.Trip
import app.nighttrain.domain.Journal
import app.nighttrain.domain.Rails
import app.nighttrain.ui.Depot
import app.nighttrain.ui.Go
import app.nighttrain.ui.T
import app.nighttrain.ui.art.Ticket
import app.nighttrain.ui.art.TrainWindow
import app.nighttrain.ui.components.PrimaryButton
import app.nighttrain.ui.components.SectionTitle
import app.nighttrain.ui.components.SoftButton
import app.nighttrain.ui.components.Stat
import app.nighttrain.ui.components.UCard
import app.nighttrain.ui.components.UTextField
import app.nighttrain.ui.ctx
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType

/** Morning at the destination: the ticket as punched, the trip in numbers, and a note to leave in the compartment. */
@Composable
fun ArrivalScreen(trip: Trip, ride: Ride, d: Depot, go: Go) {
    val c = ctx()
    val container = c.container
    val moments = d.moments(trip.id)
    val sum = Journal.summary(moments)
    val fresh = trip.to !in Journal.visited(d.trips.filter { it.id != trip.id })
    val notes = remember { mutableStateListOf("", "") }
    var writing by remember { mutableStateOf(-1) }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            TrainWindow(Rails.biome(trip.from, trip.to), Stage.DAWN, Modifier.fillMaxWidth().aspectRatio(1.4f), station = T.city(c, trip.to), seed = trip.seed.toInt())
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.arr_eyebrow).uppercase(), style = UType.Eyebrow)
            Text(T.city(c, trip.to), style = UType.Hero)
            if (fresh) Text(stringResource(R.string.arr_new), style = UType.Muted.copy(color = U.c.brass))
            Spacer(Modifier.height(14.dp))
            UCard {
                Row {
                    Stat(stringResource(R.string.stat_questions), sum.questions.toString(), Modifier.weight(1f))
                    Stat(stringResource(R.string.stat_holes), if (sum.guesses == 0) "—" else "${sum.hits}/${sum.guesses}", Modifier.weight(1f))
                    Stat(stringResource(R.string.stat_dining), if (sum.dining == 0) "—" else "${sum.matches}/${sum.dining}", Modifier.weight(1f))
                }
                if (sum.starred > 0) {
                    Spacer(Modifier.height(10.dp))
                    Text(T.plural(c, R.plurals.postcards_kept, sum.starred), style = UType.Muted.copy(color = U.c.brass))
                }
            }
            Spacer(Modifier.height(14.dp))
            Ticket(
                from = T.city(c, trip.from), to = T.city(c, trip.to), seed = trip.seed, date = trip.startedAt,
                train = T.length(c, trip.lengthOf), holes = sum.hits, modifier = Modifier.fillMaxWidth(),
            )

            SectionTitle(stringResource(R.string.arr_notes))
            Text(stringResource(R.string.arr_notes_sub), style = UType.Muted)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (0..1).forEach { who ->
                    val done = notes[who].isNotBlank()
                    SoftButton(
                        stringResource(R.string.arr_note_from, T.passenger(c, d.settings, who)),
                        { writing = who },
                        Modifier.weight(1f),
                        icon = if (done) Icons.Rounded.Check else Icons.Rounded.Edit,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        PrimaryButton(
            stringResource(R.string.arr_off),
            {
                val now = System.currentTimeMillis()
                val written = (0..1).filter { notes[it].isNotBlank() }.map { Note(from = it, text = notes[it].trim(), createdAt = now) }
                container.write { written.forEach { db.dao().insert(it) } }
                ride.arrive { go.home() }
            },
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 14.dp).navigationBarsPadding(),
        )
    }

    if (writing >= 0) {
        val who = writing
        var text by remember(who) { mutableStateOf(notes[who]) }
        AlertDialog(
            onDismissRequest = { writing = -1 },
            containerColor = U.c.surface,
            title = { Text(stringResource(R.string.arr_note_title, T.passenger(c, d.settings, 1 - who)), style = UType.Strong.copy(fontSize = 19.sp)) },
            text = {
                Column {
                    Text(stringResource(R.string.arr_note_text), style = UType.Sub)
                    Spacer(Modifier.height(10.dp))
                    UTextField(text, { text = it.take(600) }, placeholder = stringResource(R.string.arr_note_hint), minLines = 4)
                }
            },
            confirmButton = { TextButton({ notes[who] = text; writing = -1 }) { Text(stringResource(R.string.save), color = U.c.brass, style = UType.Button) } },
            dismissButton = { TextButton({ writing = -1 }) { Text(stringResource(R.string.cancel), color = U.c.muted, style = UType.Button) } },
        )
    }
}
