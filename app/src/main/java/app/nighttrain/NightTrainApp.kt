package app.nighttrain

import android.app.Application
import android.content.Context
import app.nighttrain.data.DemoSeed
import app.nighttrain.data.Deck
import app.nighttrain.data.SettingsStore
import app.nighttrain.data.TrainDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(private val context: Context) {
    val db = TrainDatabase.create(context)
    val settings = SettingsStore(context)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Every card in both languages, read from assets once. */
    val deck: Deck by lazy { Deck.load(context.assets) }

    /** Fire-and-forget so a write survives the screen being closed right after tapping. */
    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }
}

class NightTrainApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        if (BuildConfig.DEMO) container.scope.launch { DemoSeed.seedIfEmpty(container) }
    }
}

val Context.container: AppContainer get() = (applicationContext as NightTrainApp).container

/** The screenshot build (`-Pdemo -Pshots`): the window stops moving so every capture looks the same. */
val SHOTS: Boolean get() = BuildConfig.APPLICATION_ID.endsWith(".shots")
