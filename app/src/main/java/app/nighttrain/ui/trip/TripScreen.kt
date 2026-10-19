package app.nighttrain.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.nighttrain.R
import app.nighttrain.audio.Wheels
import app.nighttrain.container
import app.nighttrain.data.Seating
import app.nighttrain.data.Stage
import app.nighttrain.domain.Plan
import app.nighttrain.domain.Rails
import app.nighttrain.domain.StopType
import app.nighttrain.ui.Depot
import app.nighttrain.ui.Go
import app.nighttrain.ui.T
import app.nighttrain.ui.art.TrainWindow
import app.nighttrain.ui.components.ConfirmDialog
import app.nighttrain.ui.ctx
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType

/**
 * The compartment. Facing each other, the phone is the table: the top half is turned to the person
 * opposite, the bottom half to you, and the window runs between. Every card shows on both halves, with
 * its buttons, so either of you can answer, skip or go on.
 */
@Composable
fun TripScreen(id: Long, d: Depot, go: Go) {
    val c = ctx()
    val trip = d.trip(id)
    if (trip == null) {
        Box(Modifier.fillMaxSize().background(U.c.bg))
        return
    }
    val container = c.container
    val ride = remember(id) { Ride(container, trip, Plan.decode(trip.plan), d.moments(id)) }
    if (ride.stops.isEmpty()) return
    val stop = ride.stop
    val biome = Rails.biome(trip.from, trip.to)
    var leaving by remember { mutableStateOf(false) }

    // The screen stays lit for the whole trip: nobody wants to tap it awake mid-story.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    val moving = stop.type !in setOf(StopType.RULE, StopType.HALT, StopType.ARRIVAL, StopType.NOTE)
    Wheels.Play(d.settings.sound && moving, tunnel = stop.stage == Stage.TUNNEL)

    // Both thumbs on the gate for a moment and the train goes in. Run here, once, not in each half of the table.
    val haptic = LocalHapticFeedback.current
    val both = ride.holding[0] && ride.holding[1]
    LaunchedEffect(both, ride.pos) {
        if (!both || stop.type !in setOf(StopType.TUNNEL, StopType.SLEEPER)) {
            ride.gate = 0f
            return@LaunchedEffect
        }
        val start = withFrameMillis { it }
        while (ride.gate < 1f) withFrameMillis { ride.gate = ((it - start) / 1400f).coerceAtMost(1f) }
        if (d.settings.haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        ride.advance()
    }

    if (stop.type == StopType.ARRIVAL) {
        ArrivalScreen(trip, ride, d, go)
        return
    }

    val station = when (stop.type) {
        StopType.RULE -> stop.rule?.let { T.ruleStation(c, it) }
        StopType.HALT -> T.halt(c, stop.extra.toIntOrNull() ?: 0)
        StopType.NOTE -> T.city(c, trip.from)
        else -> null
    }
    val dark = stop.stage == Stage.TUNNEL
    val controls: @Composable (Modifier) -> Unit = { m ->
        Row(m.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(Icons.Rounded.Close, stringResource(R.string.trip_leave)) { go.home() }
            Spacer(Modifier.weight(1f))
            Text(
                "${ride.pos + 1} / ${ride.stops.size}",
                style = UType.Small.copy(color = U.c.paper),
                modifier = Modifier.clip(U.RSm).background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 8.dp, vertical = 3.dp),
            )
            Spacer(Modifier.weight(1f))
            RoundIcon(Icons.Rounded.Flag, stringResource(R.string.trip_terminus)) { leaving = true }
        }
    }
    val seed = trip.seed.toInt()

    Box(Modifier.fillMaxSize().background(if (dark) U.c.deep else U.c.bg)) {
        if (d.settings.seating == Seating.OPPOSITE) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Panel(ride, d, owner = 1, dark = dark, modifier = Modifier.weight(1f).rotate(180f))
                // Two windows back to back, one for each of you: sitting face to face, you each watch
                // your own side of the line go by, running the opposite way.
                Box(Modifier.fillMaxWidth().height(236.dp)) {
                    Column(Modifier.fillMaxSize()) {
                        TrainWindow(biome, stop.stage, Modifier.weight(1f).fillMaxWidth().rotate(180f), moving = moving, station = station, frame = false, seed = seed + 1)
                        Box(Modifier.fillMaxWidth().height(2.dp).background(U.c.brass.copy(alpha = 0.7f)))
                        TrainWindow(biome, stop.stage, Modifier.weight(1f).fillMaxWidth(), moving = moving, station = station, frame = false, seed = seed)
                    }
                    controls(Modifier.align(Alignment.Center))
                }
                Panel(ride, d, owner = 0, dark = dark, modifier = Modifier.weight(1f))
            }
        } else {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Box(Modifier.fillMaxWidth().aspectRatio(1.55f)) {
                    TrainWindow(biome, stop.stage, Modifier.fillMaxSize(), moving = moving, station = station, seed = seed)
                    controls(Modifier.padding(top = 6.dp))
                }
                Panel(ride, d, owner = -1, dark = dark, modifier = Modifier.weight(1f))
            }
        }
    }
    if (leaving) {
        ConfirmDialog(
            stringResource(R.string.trip_terminus_title),
            stringResource(R.string.trip_terminus_text, T.city(c, trip.to)),
            stringResource(R.string.trip_terminus_go),
            { ride.terminus() },
            { leaving = false },
        )
    }
}

/** One half of the table: the card as seen by [owner] (0 or 1; -1 when sitting side by side). */
@Composable
private fun Panel(ride: Ride, d: Depot, owner: Int, dark: Boolean, modifier: Modifier) {
    val top = if (dark) Color(0xFF0B0B10) else U.c.surface
    val bottom = if (dark) U.c.deep else U.c.bg
    Column(
        modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Face(ride, d, owner)
    }
}

@Composable
private fun RoundIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick, Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))) {
        Icon(icon, label, Modifier.size(18.dp), tint = U.c.paper)
    }
}
