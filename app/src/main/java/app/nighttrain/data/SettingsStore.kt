package app.nighttrain.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

/** How the two of you sit around the phone. */
enum class Seating {
    /** Facing each other across the table: each card turns to whoever it's for. */
    OPPOSITE,

    /** Side by side: one way up for both. */
    SIDE,
}

data class Settings(
    val onboarded: Boolean = false,
    /** The two passengers' names, as they'd like to be called. */
    val nameA: String = "",
    val nameB: String = "",
    val seating: Seating = Seating.OPPOSITE,
    /** The wheels on the rails, while the train is moving. */
    val sound: Boolean = true,
    val haptics: Boolean = true,
    /** The 18+ sleeping car can be offered after the tunnel. */
    val sleeper: Boolean = false,
) {
    fun name(who: Int, fallbackA: String, fallbackB: String): String = when (who) {
        0 -> nameA.ifBlank { fallbackA }
        else -> nameB.ifBlank { fallbackB }
    }
}

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        nameA = this[NAME_A] ?: "",
        nameB = this[NAME_B] ?: "",
        seating = runCatching { Seating.valueOf(this[SEATING] ?: "") }.getOrDefault(Seating.OPPOSITE),
        sound = this[SOUND] ?: true,
        haptics = this[HAPTICS] ?: true,
        sleeper = this[SLEEPER] ?: false,
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[NAME_A] = s.nameA
            it[NAME_B] = s.nameB
            it[SEATING] = s.seating.name
            it[SOUND] = s.sound
            it[HAPTICS] = s.haptics
            it[SLEEPER] = s.sleeper
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val NAME_A = stringPreferencesKey("name_a")
        val NAME_B = stringPreferencesKey("name_b")
        val SEATING = stringPreferencesKey("seating")
        val SOUND = booleanPreferencesKey("sound")
        val HAPTICS = booleanPreferencesKey("haptics")
        val SLEEPER = booleanPreferencesKey("sleeper")
    }
}
