package app.nighttrain.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.nighttrain.R
import app.nighttrain.data.Seating
import app.nighttrain.data.Settings
import app.nighttrain.data.Stage
import app.nighttrain.domain.Biome
import app.nighttrain.ui.art.TrainWindow
import app.nighttrain.ui.components.PrimaryButton
import app.nighttrain.ui.components.UTextField
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType

/** Three steps: the idea, your names, how you sit. */
@Composable
fun OnboardingScreen(settings: Settings, onDone: (String, String, Seating) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var a by rememberSaveable { mutableStateOf(settings.nameA) }
    var b by rememberSaveable { mutableStateOf(settings.nameB) }
    var seating by rememberSaveable { mutableStateOf(settings.seating) }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(14.dp))
            TrainWindow(
                if (step == 0) Biome.FOREST else if (step == 1) Biome.FIELDS else Biome.SEA,
                if (step == 0) Stage.EVENING else Stage.NIGHT,
                Modifier.fillMaxWidth().aspectRatio(1.25f),
            )
            Spacer(Modifier.height(22.dp))
            when (step) {
                0 -> {
                    Text(stringResource(R.string.ob_eyebrow).uppercase(), style = UType.Eyebrow)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.ob_title), style = UType.Display)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.ob_text), style = UType.Sub.copy(color = U.c.ink))
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.ob_text2), style = UType.Sub)
                }
                1 -> {
                    Text(stringResource(R.string.ob_names_title), style = UType.Title)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.ob_names_text), style = UType.Sub)
                    Spacer(Modifier.height(16.dp))
                    UTextField(a, { a = it.take(24) }, placeholder = stringResource(R.string.passenger_a))
                    Spacer(Modifier.height(10.dp))
                    UTextField(b, { b = it.take(24) }, placeholder = stringResource(R.string.passenger_b))
                }
                else -> {
                    Text(stringResource(R.string.ob_seat_title), style = UType.Title)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.ob_seat_text), style = UType.Sub)
                    Spacer(Modifier.height(16.dp))
                    SeatingPicker(seating) { seating = it }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                Box(Modifier.weight(1f).height(4.dp).clip(U.RSm).background(if (i <= step) U.c.brass else U.c.line))
            }
        }
        PrimaryButton(
            stringResource(if (step < 2) R.string.next else R.string.ob_board),
            { if (step < 2) step++ else onDone(a.trim(), b.trim(), seating) },
            Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 16.dp),
        )
    }
}

/** Facing each other or side by side, as two cards. */
@Composable
fun SeatingPicker(selected: Seating, onPick: (Seating) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Seating.entries.forEach { s ->
            val on = s == selected
            Column(
                Modifier.fillMaxWidth().clip(U.RMd)
                    .background(if (on) U.c.brassSoft else U.c.surface)
                    .border(1.dp, if (on) U.c.brass.copy(alpha = 0.6f) else U.c.line, U.RMd)
                    .clickable { onPick(s) }
                    .padding(16.dp),
            ) {
                Text(stringResource(if (s == Seating.OPPOSITE) R.string.seat_opposite else R.string.seat_side), style = UType.Strong.copy(color = if (on) U.c.brass else U.c.ink))
                Spacer(Modifier.height(4.dp))
                Text(stringResource(if (s == Seating.OPPOSITE) R.string.seat_opposite_sub else R.string.seat_side_sub), style = UType.Muted)
            }
        }
    }
}
