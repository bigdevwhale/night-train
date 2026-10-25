package app.nighttrain.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.nighttrain.R
import app.nighttrain.container
import app.nighttrain.data.Backup
import app.nighttrain.data.Everything
import app.nighttrain.ui.Depot
import app.nighttrain.ui.Go
import app.nighttrain.ui.Language
import app.nighttrain.ui.components.Chip
import app.nighttrain.ui.components.CommunityLinks
import app.nighttrain.ui.components.ConfirmDialog
import app.nighttrain.ui.components.InfoRow
import app.nighttrain.ui.components.SectionTitle
import app.nighttrain.ui.components.TopBar
import app.nighttrain.ui.components.UCard
import app.nighttrain.ui.components.UTextField
import app.nighttrain.ui.ctx
import app.nighttrain.ui.onboarding.SeatingPicker
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.theme.UType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(d: Depot, go: Go) {
    val c = ctx()
    val container = c.container
    val s = d.settings
    val scope = rememberCoroutineScope()
    var restoring by remember { mutableStateOf<android.net.Uri?>(null) }
    var a by remember { mutableStateOf(s.nameA) }
    var b by remember { mutableStateOf(s.nameB) }

    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val ok = withContext(Dispatchers.IO) { runCatching { Backup.write(c, uri, Everything(d.trips, d.moments, d.notes)) }.isSuccess }
            Toast.makeText(c, if (ok) R.string.backup_done else R.string.backup_failed, Toast.LENGTH_SHORT).show()
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> restoring = uri }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().imePadding()) {
        TopBar(stringResource(R.string.settings), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding()) {
            SectionTitle(stringResource(R.string.set_passengers))
            UTextField(a, { a = it.take(24); container.write { settings.update { s -> s.copy(nameA = a.trim()) } } }, placeholder = stringResource(R.string.passenger_a))
            Spacer(Modifier.height(8.dp))
            UTextField(b, { b = it.take(24); container.write { settings.update { s -> s.copy(nameB = b.trim()) } } }, placeholder = stringResource(R.string.passenger_b))

            SectionTitle(stringResource(R.string.set_seating))
            SeatingPicker(s.seating) { v -> container.write { settings.update { it.copy(seating = v) } } }

            SectionTitle(stringResource(R.string.set_train))
            UCard {
                Toggle(stringResource(R.string.set_sound), stringResource(R.string.set_sound_sub), s.sound) { v -> container.write { settings.update { it.copy(sound = v) } } }
                Spacer(Modifier.height(12.dp))
                Toggle(stringResource(R.string.set_haptics), stringResource(R.string.set_haptics_sub), s.haptics) { v -> container.write { settings.update { it.copy(haptics = v) } } }
                Spacer(Modifier.height(12.dp))
                Toggle(stringResource(R.string.set_sleeper), stringResource(R.string.set_sleeper_sub), s.sleeper) { v -> container.write { settings.update { it.copy(sleeper = v) } } }
            }

            SectionTitle(stringResource(R.string.set_language))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val cur = Language.current()
                Chip(stringResource(R.string.set_lang_system), cur == Language.SYSTEM, { Language.set(Language.SYSTEM) })
                Chip("English", cur == Language.EN, { Language.set(Language.EN) })
                Chip("Русский", cur == Language.RU, { Language.set(Language.RU) })
            }

            SectionTitle(stringResource(R.string.set_data))
            UCard {
                InfoRow(Icons.Rounded.Upload, stringResource(R.string.backup_export_sub), stringResource(R.string.backup_export)) { export.launch("night-train-${LocalDate.now()}.json") }
                InfoRow(Icons.Rounded.Download, stringResource(R.string.backup_import_sub), stringResource(R.string.backup_import)) { import.launch(arrayOf("application/json", "text/plain", "*/*")) }
            }

            CommunityLinks()

            SectionTitle(stringResource(R.string.set_about))
            Text(stringResource(R.string.set_method), style = UType.Sub)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.set_deck, d.deck.cards.size), style = UType.Sub.copy(color = U.c.ink))
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.set_privacy), style = UType.Muted)
            Spacer(Modifier.height(24.dp))
        }
    }
    restoring?.let { uri ->
        ConfirmDialog(
            stringResource(R.string.backup_import_title),
            stringResource(R.string.backup_import_text),
            stringResource(R.string.backup_import_go),
            {
                scope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        runCatching {
                            val all = Backup.read(c, uri)
                            container.db.dao().replaceAll(all)
                            container.settings.update { it.copy(onboarded = true) }
                        }.isSuccess
                    }
                    Toast.makeText(c, if (ok) R.string.backup_restored else R.string.backup_bad, Toast.LENGTH_SHORT).show()
                }
            },
            { restoring = null },
        )
    }
}

@Composable
private fun Toggle(title: String, sub: String, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = UType.Strong)
            Text(sub, style = UType.Muted)
        }
        Switch(on, onChange, colors = SwitchDefaults.colors(checkedTrackColor = U.c.brass, checkedThumbColor = U.c.deep))
    }
}
