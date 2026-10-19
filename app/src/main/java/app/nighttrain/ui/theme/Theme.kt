package app.nighttrain.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.nighttrain.R

/**
 * A sleeping car at night: navy dark behind the window, polished mahogany, brass fittings, the warm lamp
 * over the table, cream ticket paper. Velvet red only for the seats and the emergency brake.
 */
@Immutable
data class Palette(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val ink: Color,
    val muted: Color,
    val faint: Color,
    val brass: Color,
    val brassSoft: Color,
    val lamp: Color,
    val wood: Color,
    val woodDark: Color,
    val velvet: Color,
    val paper: Color,
    val paperInk: Color,
    val deep: Color,
    val dawn: Color,
    val green: Color,
)

val CarPalette = Palette(
    bg = Color(0xFF0D1220),
    surface = Color(0xFF151C2E),
    surface2 = Color(0xFF1C2439),
    line = Color(0xFF2A3350),
    ink = Color(0xFFF1E8D6),
    muted = Color(0xFFADB2C4),
    faint = Color(0xFF6F7690),
    brass = Color(0xFFD6AE62),
    brassSoft = Color(0x2BD6AE62),
    lamp = Color(0xFFF6CB78),
    wood = Color(0xFF6B3523),
    woodDark = Color(0xFF3E1D13),
    velvet = Color(0xFFA03A40),
    paper = Color(0xFFEFE3C8),
    paperInk = Color(0xFF2B2219),
    deep = Color(0xFF070A13),
    dawn = Color(0xFFEFA48C),
    green = Color(0xFF8CC8A0),
)

val LocalPalette = staticCompositionLocalOf { CarPalette }

/** Prata: names of cities and stations, as on a 1930s timetable. */
val DisplayFont = FontFamily(Font(R.font.prata, FontWeight.Normal))

/** Lora: what the conductor and the passengers say. */
val Serif = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_medium, FontWeight.Medium),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
)

/** Manrope for buttons and labels. */
val Sans = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

/** PT Mono for tickets: numbers, dates, carriage and seat. */
val Mono = FontFamily(Font(R.font.pt_mono, FontWeight.Normal))

object U {
    val c: Palette @Composable get() = LocalPalette.current

    val RXl = RoundedCornerShape(24.dp)
    val RLg = RoundedCornerShape(18.dp)
    val RMd = RoundedCornerShape(14.dp)
    val RSm = RoundedCornerShape(10.dp)
}

object UType {
    val Hero: TextStyle @Composable get() = TextStyle(fontFamily = DisplayFont, fontSize = 38.sp, lineHeight = 44.sp, color = U.c.ink)
    val Display: TextStyle @Composable get() = TextStyle(fontFamily = DisplayFont, fontSize = 29.sp, lineHeight = 35.sp, color = U.c.ink)
    val Title: TextStyle @Composable get() = TextStyle(fontFamily = DisplayFont, fontSize = 22.sp, lineHeight = 28.sp, color = U.c.ink)
    val Name: TextStyle @Composable get() = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 23.sp, color = U.c.ink)
    /** A question, read across the table. */
    val Question: TextStyle @Composable get() = TextStyle(fontFamily = Serif, fontSize = 23.sp, lineHeight = 31.sp, color = U.c.ink)
    val Quote: TextStyle @Composable get() = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 17.sp, lineHeight = 24.sp, color = U.c.paper)
    val Sub: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontSize = 15.sp, lineHeight = 22.sp, color = U.c.muted)
    val Eyebrow: TextStyle @Composable get() = TextStyle(fontFamily = Mono, fontSize = 11.sp, letterSpacing = 0.14.em, color = U.c.faint)
    val Strong: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = U.c.ink)
    val Body: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontSize = 15.5.sp, lineHeight = 23.sp, color = U.c.ink)
    val Muted: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontSize = 13.5.sp, lineHeight = 19.sp, color = U.c.muted)
    val Small: TextStyle @Composable get() = TextStyle(fontFamily = Mono, fontSize = 11.5.sp, color = U.c.faint)
    val Ticket: TextStyle @Composable get() = TextStyle(fontFamily = Mono, fontSize = 13.sp, lineHeight = 18.sp, color = U.c.paperInk)
    val Number: TextStyle @Composable get() = TextStyle(fontFamily = DisplayFont, fontSize = 27.sp, color = U.c.ink)
    val Button: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
}

@Composable
fun TrainTheme(content: @Composable () -> Unit) {
    val p = CarPalette
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = p.brass,
                onPrimary = p.deep,
                primaryContainer = p.brassSoft,
                onPrimaryContainer = p.ink,
                secondary = p.lamp,
                background = p.bg,
                surface = p.surface,
                surfaceContainer = p.surface,
                surfaceContainerHigh = p.surface2,
                surfaceContainerHighest = p.surface2,
                surfaceContainerLow = p.surface,
                onBackground = p.ink,
                onSurface = p.ink,
                onSurfaceVariant = p.muted,
                outline = p.line,
                outlineVariant = p.line,
                error = p.velvet,
            ),
            typography = MaterialTheme.typography.copy(
                bodyLarge = UType.Body,
                bodyMedium = UType.Body.copy(fontSize = 15.sp),
                labelLarge = UType.Button,
            ),
            content = content,
        )
    }
}
