package app.nighttrain.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import app.nighttrain.SHOTS
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * The wheels on the rail joints: a low rumble with the "ta-dum, ta-dum" of two bogies crossing a joint,
 * synthesised once into a short loop. Quiet, a little louder and boomier in the tunnel.
 */
object Wheels {
    private const val RATE = 22_050
    private const val LOOP_S = 2.4f

    /** One loop of 16-bit mono samples. */
    fun synth(seed: Int = 7): ShortArray {
        val n = (RATE * LOOP_S).toInt()
        val out = FloatArray(n)
        val rng = Random(seed)
        // Brown-ish rumble: integrated noise, leaking back to zero.
        var brown = 0f
        for (i in 0 until n) {
            brown = (brown + (rng.nextFloat() * 2 - 1) * 0.02f) * 0.995f
            out[i] = brown * 1.6f
        }
        // Two joint clacks per bogie pair: at 0.0 s and 0.16 s, again at 1.2 s and 1.36 s.
        for (at in floatArrayOf(0.02f, 0.18f, 1.22f, 1.38f)) {
            val start = (at * RATE).toInt()
            for (j in 0 until (RATE * 0.22f).toInt()) {
                val i = start + j
                if (i >= n) break
                val t = j / RATE.toFloat()
                val thump = sin(2 * PI * 62.0 * t).toFloat() * exp(-t * 26.0).toFloat() * 0.55f
                val click = (rng.nextFloat() * 2 - 1) * exp(-t * 140.0).toFloat() * 0.35f
                out[i] += thump + click
            }
        }
        // Fade the loop's ends into each other so the seam doesn't click.
        val fade = (RATE * 0.01f).toInt()
        for (i in 0 until fade) {
            val k = i / fade.toFloat()
            out[i] *= k
            out[n - 1 - i] *= k
        }
        return ShortArray(n) { (out[it].coerceIn(-1f, 1f) * 32767 * 0.8f).toInt().toShort() }
    }

    private class Player {
        private val samples = synth()
        private val track: AudioTrack? = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
                .also {
                    it.write(samples, 0, samples.size)
                    it.setLoopPoints(0, samples.size, -1)
                }
        }.getOrNull()

        fun set(on: Boolean, volume: Float) {
            val t = track ?: return
            runCatching {
                t.setVolume(volume)
                if (on && t.playState != AudioTrack.PLAYSTATE_PLAYING) t.play()
                if (!on && t.playState == AudioTrack.PLAYSTATE_PLAYING) t.pause()
            }
        }

        fun release() = runCatching { track?.release() }
    }

    /** Plays while [on] and the screen is in front. */
    @Composable
    fun Play(on: Boolean, tunnel: Boolean) {
        if (SHOTS) return
        val player = remember { Player() }
        val visible = remember { booleanArrayOf(true) }
        DisposableEffect(Unit) { onDispose { player.release() } }
        LaunchedEffect(on, tunnel) { player.set(on && visible[0], if (tunnel) 0.55f else 0.3f) }
        LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { visible[0] = false; player.set(false, 0f) }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { visible[0] = true; player.set(on, if (tunnel) 0.55f else 0.3f) }
    }
}
