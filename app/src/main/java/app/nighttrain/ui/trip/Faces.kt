package app.nighttrain.ui.trip

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Sailing
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nighttrain.R
import app.nighttrain.SHOTS
import app.nighttrain.data.Kind
import app.nighttrain.data.Result
import app.nighttrain.data.Stage
import app.nighttrain.domain.Director
import app.nighttrain.domain.Rule
import app.nighttrain.domain.StopType
import app.nighttrain.ui.Depot
import app.nighttrain.ui.T
import app.nighttrain.ui.components.Bar
import app.nighttrain.ui.components.PrimaryButton
import app.nighttrain.ui.components.SoftButton
import app.nighttrain.ui.components.UTextField
import app.nighttrain.ui.ctx
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType
import kotlinx.coroutines.delay

/** What the current stop looks like from [owner]'s side of the table. */
@Composable
fun Face(ride: Ride, d: Depot, owner: Int) {
    when (ride.stop.type) {
        StopType.QUESTION -> QuestionFace(ride, d, owner)
        StopType.ECHO -> QuestionFace(ride, d, owner, echo = true)
        StopType.GUESS -> GuessFace(ride, d, owner)
        StopType.RULE -> RuleFace(ride)
        StopType.HALT -> HaltFace(ride, d)
        StopType.DINING -> DiningFace(ride, d)
        StopType.STRANGER -> StrangerFace(ride, d, owner)
        StopType.TUNNEL, StopType.SLEEPER -> GateFace(ride, d, owner)
        StopType.NOTE -> NoteFace(ride, d, owner)
        StopType.LIE -> LieFace(ride)
        StopType.ARRIVAL -> Unit
    }
}

@Composable
private fun stageName(s: Stage): String = stringResource(
    when (s) {
        Stage.EVENING, Stage.ANY -> R.string.stage_evening
        Stage.NIGHT -> R.string.stage_night
        Stage.TUNNEL -> R.string.stage_tunnel
        Stage.DAWN -> R.string.stage_dawn
    }
)

/** "To you, Anna", "Boris answers", "Both of you". */
@Composable
private fun whoLabel(d: Depot, owner: Int, to: Int): String {
    val c = ctx()
    return when {
        to == 2 -> stringResource(R.string.who_both)
        owner == to -> stringResource(R.string.who_you, T.passenger(c, d.settings, to))
        else -> stringResource(R.string.who_answers, T.passenger(c, d.settings, to))
    }
}

@Composable
private fun Header(eyebrow: String, who: String?, mine: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(eyebrow.uppercase(), style = UType.Eyebrow, modifier = Modifier.weight(1f), maxLines = 1)
        if (who != null) {
            Text(
                who,
                style = UType.Small.copy(color = if (mine) U.c.deep else U.c.brass, fontSize = 11.sp),
                modifier = Modifier.clip(U.RSm).background(if (mine) U.c.brass else U.c.brassSoft).padding(horizontal = 8.dp, vertical = 3.dp),
                maxLines = 1,
            )
        }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun Big(text: String, dark: Boolean = false) {
    Text(text, style = UType.Question.copy(color = if (dark) U.c.lamp.copy(alpha = 0.92f) else U.c.ink))
}

@Composable
private fun RuleChip(rule: Rule) {
    val c = ctx()
    Spacer(Modifier.height(10.dp))
    Text(
        T.ruleShort(c, rule),
        style = UType.Muted.copy(color = U.c.lamp),
        modifier = Modifier.clip(U.RSm).background(U.c.brassSoft).padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

/** A small round action with a word under it. */
@Composable
private fun Act(icon: ImageVector, label: String, onClick: () -> Unit, tint: Color = U.c.muted, on: Boolean = false) {
    Column(Modifier.width(62.dp).clip(U.RSm).clickable(onClick = onClick).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(if (on) U.c.brassSoft else U.c.surface2), contentAlignment = Alignment.Center) {
            Icon(icon, label, Modifier.size(20.dp), tint = if (on) U.c.brass else tint)
        }
        Spacer(Modifier.height(3.dp))
        Text(label, style = UType.Small.copy(fontSize = 10.sp), maxLines = 1, textAlign = TextAlign.Center)
    }
}

/** Under a question: the emergency brake, "deeper", a return ticket, a postcard, and on. */
@Composable
private fun Controls(ride: Ride, d: Depot, onNext: () -> Unit, onSkip: () -> Unit, swap: Boolean = true) {
    val c = ctx()
    var keeping by remember { mutableStateOf(false) }
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Act(Icons.Rounded.PanTool, stringResource(R.string.act_brake), onSkip, tint = U.c.velvet)
        Act(Icons.Rounded.KeyboardDoubleArrowDown, stringResource(R.string.act_deeper), {
            val f = d.deck.of(Kind.F)
            if (f.isNotEmpty()) ride.deeper = f.filter { it.text(T.lang(c)) != ride.deeper }.random().text(T.lang(c))
        }, on = ride.deeper != null)
        if (swap) Act(Icons.Rounded.SwapVert, stringResource(R.string.act_return), { ride.swapped = !ride.swapped }, on = ride.swapped)
        Act(if (ride.starred) Icons.Rounded.Star else Icons.Rounded.StarBorder, stringResource(R.string.act_keep), {
            if (ride.starred) { ride.starred = false; ride.note = "" } else keeping = true
        }, on = ride.starred)
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(54.dp).clip(CircleShape).background(U.c.brass).clickable(onClick = onNext), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, stringResource(R.string.next), tint = U.c.deep)
        }
    }
    if (keeping) KeepDialog(ride) { keeping = false }
}

/** A postcard: the card is kept, with a line about what was said if you like. */
@Composable
private fun KeepDialog(ride: Ride, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(ride.note) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = U.c.surface,
        title = { Text(stringResource(R.string.keep_title), style = UType.Strong.copy(fontSize = 19.sp)) },
        text = {
            Column {
                Text(stringResource(R.string.keep_text), style = UType.Sub)
                Spacer(Modifier.height(10.dp))
                UTextField(text, { text = it.take(400) }, placeholder = stringResource(R.string.keep_hint), minLines = 3)
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { ride.starred = true; ride.note = text; onDismiss() }) {
                Text(stringResource(R.string.keep_go), color = U.c.brass, style = UType.Button)
            }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = U.c.muted, style = UType.Button) } },
    )
}

@Composable
private fun QuestionFace(ride: Ride, d: Depot, owner: Int, echo: Boolean = false) {
    val c = ctx()
    val stop = ride.stop
    val card = d.deck[stop.card] ?: return Skipper(ride)
    val to = ride.to()
    val dark = stop.stage == Stage.TUNNEL
    Header(
        if (echo) stringResource(R.string.echo_eyebrow) else "${stageName(stop.stage)} · ${stringResource(if (stop.zone == Director.ZONE_SLEEPER) R.string.q_sleeper else R.string.q_conductor)}",
        whoLabel(d, owner, to), owner == to || to == 2 && owner >= 0,
    )
    if (echo) {
        val past = stop.extra.toLongOrNull()?.let { id -> d.moments.firstOrNull { it.id == id } }
        if (past != null) {
            Text(stringResource(R.string.echo_when, T.date(c, past.at)), style = UType.Muted)
            if (past.note.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(T.quote(c, past.note), style = UType.Quote)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    Big(card.text(T.lang(c)), dark)
    stop.rule?.let { RuleChip(it) }
    if (stop.rule == Rule.SILENCE) Silence(ride.pos)
    if (echo) {
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.echo_ask), style = UType.Muted.copy(color = U.c.lamp))
    }
    ride.deeper?.let {
        Spacer(Modifier.height(12.dp))
        Text(it, style = UType.Quote.copy(color = U.c.lamp))
    }
    Controls(
        ride, d,
        onNext = { ride.record(card.id, to, Result.DONE); ride.advance() },
        onSkip = { ride.record(card.id, to, Result.SKIPPED, star = false); ride.advance() },
        swap = to != 2,
    )
}

/** Ten seconds of quiet before answering, as a draining line. */
@Composable
private fun Silence(key: Int) {
    val left = remember(key) { Animatable(1f) }
    LaunchedEffect(key) { if (!SHOTS) left.animateTo(0f, tween(10_000, easing = LinearEasing)) }
    Spacer(Modifier.height(10.dp))
    Bar(left.value, color = U.c.lamp)
}

/** A card that's gone from the deck (an old trip after an update): pass straight on. */
@Composable
private fun Skipper(ride: Ride) {
    LaunchedEffect(ride.pos) { ride.advance() }
}

@Composable
private fun GuessFace(ride: Ride, d: Depot, owner: Int) {
    val c = ctx()
    val haptic = LocalHapticFeedback.current
    val stop = ride.stop
    val card = d.deck[stop.card] ?: return Skipper(ride)
    val to = stop.to
    val guesser = 1 - to
    val name = T.passenger(c, d.settings, to)
    Header(
        "${stageName(stop.stage)} · ${stringResource(R.string.guess_eyebrow)}",
        if (ride.phase == 0) stringResource(R.string.who_guesses, T.passenger(c, d.settings, guesser)) else whoLabel(d, owner, to),
        owner == (if (ride.phase == 0) guesser else to),
    )
    Text(
        if (ride.phase == 0) stringResource(R.string.guess_first, T.passenger(c, d.settings, guesser), name) else stringResource(R.string.guess_now, name),
        style = UType.Muted.copy(color = U.c.lamp),
    )
    Spacer(Modifier.height(8.dp))
    Big(card.text(T.lang(c)))
    Spacer(Modifier.height(16.dp))
    if (ride.phase == 0) {
        PrimaryButton(stringResource(R.string.guess_said), { ride.phase = 1 }, Modifier.fillMaxWidth())
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Result.HIT to R.string.guess_hit, Result.NEAR to R.string.guess_near, Result.MISS to R.string.guess_miss).forEach { (r, label) ->
                SoftButton(stringResource(label), {
                    if (r == Result.HIT && d.settings.haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    ride.record(card.id, to, r)
                    ride.advance()
                }, Modifier.weight(1f), color = if (r == Result.HIT) U.c.brass else U.c.muted, soft = if (r == Result.HIT) U.c.brassSoft else U.c.surface2)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.guess_miss_note), style = UType.Small)
    }
}

@Composable
private fun RuleFace(ride: Ride) {
    val c = ctx()
    val rule = ride.stop.rule ?: return Skipper(ride)
    Header(stringResource(R.string.rule_eyebrow), null, false)
    Text(T.ruleStation(c, rule), style = UType.Display)
    Spacer(Modifier.height(10.dp))
    Text(T.rule(c, rule), style = UType.Body.copy(fontSize = 17.sp, lineHeight = 25.sp))
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.rule_span, Director.RULE_SPAN), style = UType.Muted)
    Spacer(Modifier.height(16.dp))
    PrimaryButton(stringResource(R.string.rule_go), { ride.advance() }, Modifier.fillMaxWidth())
}

@Composable
private fun HaltFace(ride: Ride, d: Depot) {
    val c = ctx()
    val stop = ride.stop
    val card = d.deck[stop.card] ?: return Skipper(ride)
    Header("${stringResource(R.string.halt_eyebrow)} · ${T.halt(c, stop.extra.toIntOrNull() ?: 0)}", stringResource(R.string.who_both), true)
    Text(stringResource(R.string.halt_lead), style = UType.Muted.copy(color = U.c.lamp))
    Spacer(Modifier.height(8.dp))
    Big(card.text(T.lang(c)))
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SoftButton(stringResource(R.string.halt_skip), { ride.record(card.id, 2, Result.SKIPPED); ride.advance() }, Modifier.weight(1f), color = U.c.muted, soft = U.c.surface2)
        SoftButton(stringResource(R.string.halt_done), { ride.record(card.id, 2, Result.DONE); ride.advance() }, Modifier.weight(1f))
    }
}

@Composable
private fun DiningFace(ride: Ride, d: Depot) {
    val c = ctx()
    val stop = ride.stop
    val cards = stop.cards.mapNotNull { d.deck[it] }
    if (cards.isEmpty()) return Skipper(ride)
    Header("${stringResource(R.string.dining_eyebrow)} · ${(ride.index + 1).coerceAtMost(cards.size)} / ${cards.size}", stringResource(R.string.who_both), true)
    if (ride.index >= cards.size) {
        Text(stringResource(R.string.dining_total, ride.matches, cards.size), style = UType.Display)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(if (ride.matches * 2 >= cards.size) R.string.dining_high else R.string.dining_low), style = UType.Sub)
        Spacer(Modifier.height(16.dp))
        PrimaryButton(stringResource(R.string.dining_leave), { ride.advance() }, Modifier.fillMaxWidth())
        return
    }
    val card = cards[ride.index]
    val (a, b) = card.sides(T.lang(c))
    if (ride.index == 0 && ride.phase == 0) {
        Text(stringResource(R.string.dining_lead), style = UType.Muted.copy(color = U.c.lamp))
        Spacer(Modifier.height(10.dp))
    }
    Option(1, a)
    Spacer(Modifier.height(8.dp))
    Option(2, b)
    Spacer(Modifier.height(16.dp))
    when (ride.phase) {
        0 -> PrimaryButton(stringResource(R.string.dining_count), { ride.phase = 1 }, Modifier.fillMaxWidth())
        1 -> {
            var n by remember(ride.index) { mutableStateOf(3) }
            LaunchedEffect(ride.index) {
                while (n > 0) { delay(700); n-- }
                ride.phase = 2
            }
            Text(if (n > 0) "$n…" else stringResource(R.string.dining_show), style = UType.Hero.copy(color = U.c.lamp), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SoftButton(stringResource(R.string.dining_split), { ride.record(card.id, 2, Result.SPLIT); ride.nextCard() }, Modifier.weight(1f), color = U.c.muted, soft = U.c.surface2)
            SoftButton(stringResource(R.string.dining_match), { ride.matches++; ride.record(card.id, 2, Result.MATCH); ride.nextCard() }, Modifier.weight(1f))
        }
    }
}

/** One side of a restaurant-car pair, with the number of fingers that picks it. */
@Composable
private fun Option(n: Int, text: String) {
    Row(
        Modifier.fillMaxWidth().clip(U.RMd).background(U.c.surface2).border(1.dp, U.c.line, U.RMd).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (n == 1) "☝" else "✌", fontSize = 22.sp, color = U.c.brass)
        Spacer(Modifier.width(12.dp))
        Text(text, style = UType.Name)
    }
}

private fun strangerIcon(key: String): ImageVector = when (key) {
    "sailor" -> Icons.Rounded.Sailing
    "child" -> Icons.Rounded.ChildCare
    "seer" -> Icons.Rounded.AutoAwesome
    "poet" -> Icons.Rounded.HistoryEdu
    "inspector" -> Icons.Rounded.Badge
    "granny" -> Icons.Rounded.BakeryDining
    "traveller" -> Icons.Rounded.HourglassTop
    else -> Icons.Rounded.SelfImprovement
}

@Composable
private fun StrangerFace(ride: Ride, d: Depot, owner: Int) {
    val c = ctx()
    val stop = ride.stop
    val key = stop.extra
    val cards = stop.cards.mapNotNull { d.deck[it] }
    if (cards.isEmpty()) return Skipper(ride)
    val inspector = key == "inspector"
    if (ride.phase == 0 && ride.index == 0) {
        Header("${stageName(stop.stage)} · ${stringResource(R.string.stranger_eyebrow)}", null, false)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(U.c.brassSoft), contentAlignment = Alignment.Center) {
                Icon(strangerIcon(key), null, Modifier.size(30.dp), tint = U.c.brass)
            }
            Spacer(Modifier.width(14.dp))
            Text(T.stranger(c, key), style = UType.Title, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Text(T.strangerIntro(c, key), style = UType.Quote)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SoftButton(stringResource(R.string.stranger_no), { ride.advance() }, Modifier.weight(1f), color = U.c.muted, soft = U.c.surface2)
            SoftButton(stringResource(R.string.stranger_yes), { ride.phase = 1 }, Modifier.weight(1f))
        }
        return
    }
    val card = cards[ride.index.coerceAtMost(cards.lastIndex)]
    val to = ride.to(if (card.both) 2 else (stop.to + ride.index) % 2)
    Header("${T.stranger(c, key)} · ${ride.index + 1} / ${cards.size}", whoLabel(d, owner, to), owner == to || to == 2 && owner >= 0)
    Row(verticalAlignment = Alignment.Top) {
        Icon(strangerIcon(key), null, Modifier.size(22.dp).padding(top = 4.dp), tint = U.c.brass)
        Spacer(Modifier.width(10.dp))
        Text(card.text(T.lang(c)), style = UType.Question.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
    }
    if (inspector) {
        val left = remember(ride.index) { Animatable(1f) }
        LaunchedEffect(ride.index) { if (!SHOTS) left.animateTo(0f, tween(5_000, easing = LinearEasing)) }
        Spacer(Modifier.height(12.dp))
        Bar(left.value, color = if (left.value > 0f) U.c.lamp else U.c.velvet)
        if (left.value == 0f) Text(stringResource(R.string.inspector_time), style = UType.Small.copy(color = U.c.velvet))
    }
    ride.deeper?.let {
        Spacer(Modifier.height(12.dp))
        Text(it, style = UType.Quote.copy(color = U.c.lamp))
    }
    val next = {
        if (ride.index >= cards.lastIndex) ride.advance() else ride.nextCard()
    }
    Controls(
        ride, d,
        onNext = { ride.record(card.id, to, Result.DONE); next() },
        onSkip = { ride.record(card.id, to, Result.SKIPPED, star = false); next() },
        swap = to != 2,
    )
}

@Composable
private fun GateFace(ride: Ride, d: Depot, owner: Int) {
    val sleeper = ride.stop.type == StopType.SLEEPER
    val accent = if (sleeper) U.c.velvet else U.c.lamp
    Header(stringResource(if (sleeper) R.string.sleeper_eyebrow else R.string.tunnel_eyebrow), stringResource(R.string.who_both), true)
    Text(stringResource(if (sleeper) R.string.sleeper_title else R.string.tunnel_title), style = UType.Title)
    Spacer(Modifier.height(8.dp))
    Text(stringResource(if (sleeper) R.string.sleeper_text else R.string.tunnel_text), style = UType.Sub)
    Spacer(Modifier.height(16.dp))

    val round = stringResource(if (sleeper) R.string.sleeper_no else R.string.tunnel_no)
    if (owner >= 0) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            HoldPad(T.passenger(ctx(), d.settings, owner), ride.holding[owner], ride.gate, accent, Modifier.weight(1.5f)) { ride.holding[owner] = it }
            SoftButton(round, { ride.advance(decline = true) }, Modifier.weight(1f), color = U.c.muted, soft = U.c.surface2)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0..1).forEach { who ->
                HoldPad(T.passenger(ctx(), d.settings, who), ride.holding[who], ride.gate, accent, Modifier.weight(1f)) { ride.holding[who] = it }
            }
        }
        Spacer(Modifier.height(10.dp))
        SoftButton(round, { ride.advance(decline = true) }, Modifier.fillMaxWidth(), color = U.c.muted, soft = U.c.surface2)
    }
}

@Composable
private fun NoteFace(ride: Ride, d: Depot, owner: Int) {
    val c = ctx()
    val stop = ride.stop
    val note = stop.extra.toLongOrNull()?.let { id -> d.notes.firstOrNull { it.id == id } }
    if (note == null) return Skipper(ride)
    val forName = T.passenger(c, d.settings, stop.to)
    Header(stringResource(R.string.note_eyebrow), stringResource(R.string.note_for, forName), owner == stop.to)
    if (owner == stop.to || owner < 0) {
        Text(stringResource(R.string.note_found, forName, T.passenger(c, d.settings, note.from)), style = UType.Muted.copy(color = U.c.lamp))
        Spacer(Modifier.height(10.dp))
        Text(note.text, style = UType.Quote.copy(fontSize = 20.sp, lineHeight = 28.sp))
    } else {
        Text(stringResource(R.string.note_not_yours, forName), style = UType.Sub)
    }
    Spacer(Modifier.height(16.dp))
    PrimaryButton(stringResource(R.string.note_read), { ride.advance() }, Modifier.fillMaxWidth())
}

@Composable
private fun LieFace(ride: Ride) {
    Header(stringResource(R.string.lie_eyebrow), stringResource(R.string.who_both), true)
    Big(stringResource(R.string.lie_text))
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.lie_sub), style = UType.Muted)
    Spacer(Modifier.height(16.dp))
    PrimaryButton(stringResource(R.string.lie_go), { ride.advance() }, Modifier.fillMaxWidth())
}
