package app.nighttrain.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.nighttrain.R
import app.nighttrain.ui.components.ProgressRing
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType

/** A place to rest a thumb. Lit while pressed; the ring fills once both are down. */
@Composable
fun HoldPad(name: String, held: Boolean, fill: Float, accent: Color, modifier: Modifier = Modifier, onHold: (Boolean) -> Unit) {
    Row(
        modifier.clip(U.RLg)
            .background(if (held) accent.copy(alpha = 0.18f) else U.c.surface2)
            .border(1.dp, if (held) accent else U.c.line, U.RLg)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    onHold(true)
                    waitForUpOrCancellation()
                    onHold(false)
                }
            }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProgressRing(fill, Modifier.size(50.dp), stroke = 4.dp, color = accent, track = accent.copy(alpha = 0.15f)) {
            Icon(Icons.Rounded.Fingerprint, null, Modifier.size(26.dp), tint = if (held) accent else U.c.faint)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(name, style = UType.Strong, maxLines = 1)
            Text(stringResource(if (held) R.string.gate_holding else R.string.gate_hold), style = UType.Small)
        }
    }
}
