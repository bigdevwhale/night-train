package app.nighttrain.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nighttrain.R
import app.nighttrain.data.Kind
import app.nighttrain.data.Moment
import app.nighttrain.data.Stage
import app.nighttrain.domain.Rails
import app.nighttrain.ui.Depot
import app.nighttrain.ui.Go
import app.nighttrain.ui.T
import app.nighttrain.ui.art.BiomeTile
import app.nighttrain.ui.components.Empty
import app.nighttrain.ui.components.SectionTitle
import app.nighttrain.ui.ctx
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType

/** The album: a postcard from every city reached, and the answers you kept on the way. */
@Composable
fun AlbumTab(d: Depot, @Suppress("UNUSED_PARAMETER") go: Go) {
    val c = ctx()
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.album_eyebrow).uppercase(), style = UType.Eyebrow)
            Text(stringResource(R.string.album_title), style = UType.Display)
            SectionTitle(stringResource(R.string.album_cities, d.visited.size, Rails.cities.size))
            Cities(d)
            SectionTitle(stringResource(R.string.album_kept))
        }
        if (d.postcards.isEmpty()) {
            item { Empty(stringResource(R.string.album_empty), stringResource(R.string.album_empty_sub)) }
        } else {
            items(d.postcards, key = { it.id }) { m -> Postcard(m, d) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/** Every city as a small view, lit for the ones reached. */
@Composable
private fun Cities(d: Depot) {
    val c = ctx()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Rails.cities.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { city ->
                    val seen = city.key in d.visited
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().aspectRatio(0.9f).clip(U.RSm).border(1.dp, if (seen) U.c.brass.copy(alpha = 0.5f) else U.c.line, U.RSm)) {
                            BiomeTile(
                                Rails.departures(city.key).first().biome,
                                Modifier.fillMaxSize().alpha(if (seen) 1f else 0.18f),
                                stage = if (seen) Stage.DAWN else Stage.NIGHT,
                                seed = city.key.hashCode(),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (seen) T.city(c, city.key) else "?",
                            style = UType.Small.copy(fontSize = 10.sp, color = if (seen) U.c.ink else U.c.faint),
                            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                        )
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** A kept answer on cream card: the question, what was said, who and when. */
@Composable
private fun Postcard(m: Moment, d: Depot) {
    val c = ctx()
    val card = d.deck[m.cardId] ?: return
    val trip = d.trip(m.tripId)
    Column(Modifier.fillMaxWidth().clip(U.RMd).background(U.c.paper).padding(16.dp)) {
        Text(
            listOfNotNull(
                if (card.kind == Kind.S) T.stranger(c, card.theme) else T.to(c, d.settings, m.to),
                trip?.let { "${T.city(c, it.from)} → ${T.city(c, it.to)}" },
                T.date(c, m.at),
            ).joinToString(" · ").uppercase(),
            style = UType.Ticket.copy(fontSize = 10.sp, color = U.c.velvet),
        )
        Spacer(Modifier.height(8.dp))
        Text(card.text(T.lang(c)), style = UType.Name.copy(color = U.c.paperInk))
        if (m.note.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(T.quote(c, m.note), style = UType.Quote.copy(color = U.c.paperInk.copy(alpha = 0.85f)))
        }
    }
}
