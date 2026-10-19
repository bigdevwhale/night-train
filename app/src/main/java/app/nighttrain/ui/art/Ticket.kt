package app.nighttrain.ui.art

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nighttrain.R
import app.nighttrain.ui.T
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType

/**
 * A 1930s card ticket: cream paper with notched sides and a tear-off stub, the route in big letters,
 * carriage and seats from the seed, and one punched hole for every guess that hit.
 */
@Composable
fun Ticket(
    from: String,
    to: String,
    seed: Long,
    date: Long,
    train: String,
    holes: Int,
    modifier: Modifier = Modifier,
    note: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val paper = U.c.paper
    val ink = U.c.paperInk
    val bg = U.c.bg
    val red = U.c.velvet
    val (s1, s2) = T.seats(seed)
    Column(
        modifier
            .drawBehind {
                val r = 9.dp.toPx()
                drawRoundRect(paper, cornerRadius = CornerRadius(6.dp.toPx()))
                // The notches either side of the tear line, and the tear line itself.
                val stub = size.width * 0.74f
                drawCircle(bg, r, Offset(stub, 0f))
                drawCircle(bg, r, Offset(stub, size.height))
                drawLine(ink.copy(alpha = 0.35f), Offset(stub, r + 4), Offset(stub, size.height - r - 4), 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                // A red band down the left edge, as on old railway card tickets.
                drawRect(red.copy(alpha = 0.85f), Offset(0f, 0f), Size(7.dp.toPx(), size.height))
                drawRect(ink.copy(alpha = 0.18f), Offset(0f, 0f), size, style = Stroke(1f))
                // Punched holes for the guesses that hit, along the stub.
                val hr = 5.dp.toPx()
                repeat(holes.coerceAtMost(12)) { i ->
                    val col = i % 2
                    val row = i / 2
                    drawCircle(bg, hr, Offset(stub + (size.width - stub) * (0.33f + col * 0.34f), size.height - 16.dp.toPx() - row * (hr * 2.6f)))
                }
            }
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(0.74f).padding(end = 12.dp)) {
                Text(stringResource(R.string.ticket_head).uppercase(), style = UType.Ticket.copy(fontSize = 10.sp, color = red, letterSpacing = 1.5.sp))
                Spacer(Modifier.height(6.dp))
                Text(from, style = UType.Title.copy(color = ink, fontSize = 20.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("→ $to", style = UType.Title.copy(color = ink, fontSize = 20.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text(train.uppercase(), style = UType.Ticket.copy(fontSize = 11.sp, color = ink.copy(alpha = 0.75f)))
                Text(T.stamp(date), style = UType.Ticket.copy(fontSize = 11.sp, color = ink.copy(alpha = 0.75f)))
                if (note != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(note, style = UType.Ticket.copy(fontSize = 11.sp, color = red))
                }
            }
            Column(Modifier.weight(0.26f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.ticket_car).uppercase(), style = UType.Ticket.copy(fontSize = 9.sp, color = ink.copy(alpha = 0.6f)))
                Text("%02d".format(T.carriage(seed)), style = UType.Number.copy(color = ink, fontSize = 22.sp))
                Text(stringResource(R.string.ticket_seats).uppercase(), style = UType.Ticket.copy(fontSize = 9.sp, color = ink.copy(alpha = 0.6f)))
                Text("$s1·$s2", style = UType.Ticket.copy(fontSize = 14.sp, color = ink))
                Spacer(Modifier.height(30.dp).width(1.dp))
            }
        }
    }
}
