package app.nighttrain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType

/** A small coloured label: "HIT", "SKIPPED". */
@Composable
fun Tag(text: String, color: Color, modifier: Modifier = Modifier, soft: Color = color.copy(alpha = 0.15f)) {
    Box(modifier.clip(U.RSm).background(soft).padding(horizontal = 7.dp, vertical = 2.dp)) {
        Text(text.uppercase(), style = UType.Small.copy(color = color, fontSize = 10.5.sp), maxLines = 1)
    }
}

/** A label above a big number, for stat rows. */
@Composable
fun Stat(label: String, value: String, modifier: Modifier = Modifier, color: Color = U.c.ink, sub: String? = null) {
    Column(modifier) {
        Text(label.uppercase(), style = UType.Eyebrow, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(value, style = UType.Number.copy(color = color, fontSize = 24.sp), maxLines = 1)
        if (sub != null) Text(sub, style = UType.Small, maxLines = 2)
    }
}

/** A message for an empty screen: a picture, a title, a line. */
@Composable
fun Empty(title: String, text: String, modifier: Modifier = Modifier, art: @Composable () -> Unit = {}) {
    Column(modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        art()
        Spacer(Modifier.height(12.dp))
        Text(title, style = UType.Name, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(text, style = UType.Sub, textAlign = TextAlign.Center)
    }
}
