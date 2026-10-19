package app.nighttrain.ui.tickets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nighttrain.R
import app.nighttrain.container
import app.nighttrain.data.Kind
import app.nighttrain.data.Moment
import app.nighttrain.data.Result
import app.nighttrain.domain.Journal
import app.nighttrain.ui.Depot
import app.nighttrain.ui.Go
import app.nighttrain.ui.T
import app.nighttrain.ui.art.Ticket
import app.nighttrain.ui.components.ConfirmDialog
import app.nighttrain.ui.components.Empty
import app.nighttrain.ui.components.SectionTitle
import app.nighttrain.ui.components.Stat
import app.nighttrain.ui.components.Tag
import app.nighttrain.ui.components.TopBar
import app.nighttrain.ui.components.UCard
import app.nighttrain.ui.ctx
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType

/** The ticket book: every trip as its punched ticket, and how well you've come to guess each other. */
@Composable
fun TicketsTab(d: Depot, go: Go) {
    val c = ctx()
    val trips = d.finished.sortedByDescending { it.startedAt }
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.tickets_eyebrow).uppercase(), style = UType.Eyebrow)
            Text(stringResource(R.string.tickets_title), style = UType.Display)
        }
        if (trips.isEmpty()) {
            item { Empty(stringResource(R.string.tickets_empty), stringResource(R.string.tickets_empty_sub)) }
        } else {
            item { Trend(d) }
            items(trips, key = { it.id }) { t ->
                val sum = Journal.summary(d.moments(t.id))
                Ticket(
                    from = T.city(c, t.from), to = T.city(c, t.to), seed = t.seed, date = t.startedAt,
                    train = T.length(c, t.lengthOf), holes = sum.hits, modifier = Modifier.fillMaxWidth(),
                    note = if (sum.starred > 0) T.plural(c, R.plurals.postcards_kept, sum.starred) else null,
                    onClick = { go.ticket(t.id) },
                )
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/** Guesses that hit, trip by trip, oldest on the left: are you getting to know each other better? */
@Composable
private fun Trend(d: Depot) {
    val points = d.finished.sortedBy { it.startedAt }.mapNotNull { Journal.accuracy(d.moments(it.id)) }.takeLast(16)
    val all = Journal.accuracy(d.moments)
    UCard {
        Row(verticalAlignment = Alignment.Bottom) {
            Stat(stringResource(R.string.trend_title), all?.let { "${(it * 100).toInt()}%" } ?: "—", Modifier.weight(1f), sub = stringResource(R.string.trend_sub))
            val brass = U.c.brass
            val track = U.c.brassSoft
            Canvas(Modifier.width(150.dp).height(56.dp)) {
                if (points.isEmpty()) return@Canvas
                val bw = size.width / 16f
                points.forEachIndexed { i, p ->
                    val h = size.height * p.coerceAtLeast(0.04f)
                    drawRoundRect(track, Offset(i * bw + 2, 0f), Size(bw - 4, size.height), CornerRadius(3f))
                    drawRoundRect(brass, Offset(i * bw + 2, size.height - h), Size(bw - 4, h), CornerRadius(3f))
                }
            }
        }
    }
}

/** One trip, card by card: what came up, who answered, how it went, what was kept. */
@Composable
fun TicketScreen(id: Long, d: Depot, go: Go) {
    val c = ctx()
    val container = c.container
    val trip = d.trip(id) ?: return
    val moments = d.moments(id)
    val sum = Journal.summary(moments)
    var deleting by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        TopBar(T.date(c, trip.startedAt), go::back) {
            IconButton({ deleting = true }) { Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.delete), tint = U.c.muted) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Ticket(
                from = T.city(c, trip.from), to = T.city(c, trip.to), seed = trip.seed, date = trip.startedAt,
                train = T.length(c, trip.lengthOf), holes = sum.hits, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            UCard {
                Row {
                    Stat(stringResource(R.string.stat_questions), sum.questions.toString(), Modifier.weight(1f))
                    Stat(stringResource(R.string.stat_holes), if (sum.guesses == 0) "—" else "${sum.hits}/${sum.guesses}", Modifier.weight(1f))
                    Stat(stringResource(R.string.stat_dining), if (sum.dining == 0) "—" else "${sum.matches}/${sum.dining}", Modifier.weight(1f))
                }
                if (trip.tunnel || trip.sleeper) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        listOfNotNull(
                            stringResource(R.string.trip_through_tunnel).takeIf { trip.tunnel },
                            stringResource(R.string.trip_through_sleeper).takeIf { trip.sleeper },
                        ).joinToString(" · "),
                        style = UType.Small.copy(color = U.c.brass),
                    )
                }
            }
            SectionTitle(stringResource(R.string.ticket_cards))
            moments.forEach { m -> MomentRow(m, d) }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (deleting) {
        ConfirmDialog(
            stringResource(R.string.ticket_delete_title), stringResource(R.string.ticket_delete_text), stringResource(R.string.delete),
            { container.write { db.dao().deleteTrip(id) }; go.back() }, { deleting = false }, danger = true,
        )
    }
}

@Composable
fun MomentRow(m: Moment, d: Depot) {
    val c = ctx()
    val card = d.deck[m.cardId] ?: return
    val lang = T.lang(c)
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (card.kind == Kind.S) T.stranger(c, card.theme) else T.to(c, d.settings, m.to),
                style = UType.Small, modifier = Modifier.weight(1f), maxLines = 1,
            )
            resultTag(m.resultOf)?.let { (label, color) -> Tag(stringResource(label), color) }
            if (m.starred) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.Star, null, Modifier.size(16.dp), tint = U.c.brass)
            }
        }
        Spacer(Modifier.height(3.dp))
        val text = if (card.kind == Kind.D) card.sides(lang).let { "${it.first} / ${it.second}" } else card.text(lang)
        Text(text, style = UType.Body.copy(color = if (m.resultOf == Result.SKIPPED) U.c.faint else U.c.ink))
        if (m.note.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(T.quote(c, m.note), style = UType.Quote.copy(fontSize = 15.sp))
        }
    }
}

@Composable
private fun resultTag(r: Result): Pair<Int, androidx.compose.ui.graphics.Color>? = when (r) {
    Result.HIT -> R.string.res_hit to U.c.brass
    Result.NEAR -> R.string.res_near to U.c.lamp
    Result.MISS -> R.string.res_miss to U.c.muted
    Result.MATCH -> R.string.res_match to U.c.green
    Result.SPLIT -> R.string.res_split to U.c.muted
    Result.SKIPPED -> R.string.res_skipped to U.c.velvet
    Result.DONE -> null
}

