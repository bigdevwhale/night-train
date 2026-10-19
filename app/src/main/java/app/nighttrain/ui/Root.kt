package app.nighttrain.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.nighttrain.R
import app.nighttrain.container
import app.nighttrain.data.Deck
import app.nighttrain.data.Moment
import app.nighttrain.data.Note
import app.nighttrain.data.Settings
import app.nighttrain.data.Trip
import app.nighttrain.domain.Journal
import app.nighttrain.ui.album.AlbumTab
import app.nighttrain.ui.board.BoardScreen
import app.nighttrain.ui.map.MapTab
import app.nighttrain.ui.onboarding.OnboardingScreen
import app.nighttrain.ui.settings.SettingsScreen
import app.nighttrain.ui.station.StationTab
import app.nighttrain.ui.theme.Sans
import app.nighttrain.ui.theme.U
import app.nighttrain.ui.tickets.TicketScreen
import app.nighttrain.ui.tickets.TicketsTab
import app.nighttrain.ui.trip.TripScreen

enum class Tab { STATION, MAP, TICKETS, ALBUM }

/** Everything stored, plus what is derived from it. */
@Immutable
class Depot(
    val trips: List<Trip>,
    val moments: List<Moment>,
    val notes: List<Note>,
    val settings: Settings,
    val deck: Deck,
) {
    /** Where the train stands now. */
    val city: String by lazy { Journal.city(trips) }
    val visited: Set<String> by lazy { Journal.visited(trips) }
    val travelled: Map<String, Int> by lazy { Journal.travelled(trips) }
    val finished: List<Trip> by lazy { trips.filter { it.done } }

    /** A trip left half-way, to be picked up. */
    val ongoing: Trip? by lazy { trips.firstOrNull { !it.done } }
    val waiting: List<Note> by lazy { notes.filter { it.readAt == 0L } }

    private val byTrip: Map<Long, List<Moment>> by lazy { moments.groupBy { it.tripId } }
    fun moments(trip: Long): List<Moment> = byTrip[trip].orEmpty()
    fun trip(id: Long): Trip? = trips.firstOrNull { it.id == id }

    /** Postcards: starred moments, the newest first. */
    val postcards: List<Moment> by lazy { moments.filter { it.starred && deck[it.cardId] != null }.sortedByDescending { it.at } }
}

/** Everything a screen can open. */
class Go(private val nav: NavHostController) {
    fun board(to: String = "") = nav.navigate("board?to=$to") { launchSingleTop = true }
    fun trip(id: Long) = nav.navigate("trip/$id") {
        popUpTo("main") { inclusive = false }
        launchSingleTop = true
    }
    fun ticket(id: Long) = nav.navigate("ticket/$id") { launchSingleTop = true }
    fun settings() = nav.navigate("settings") { launchSingleTop = true }
    fun home() { nav.popBackStack("main", false) }
    fun back() { if (!nav.popBackStack()) home() }
}

@Composable
fun TrainRoot(settings: Settings) {
    val container = ctx().container
    val nav = rememberNavController()
    var tab by rememberSaveable { mutableStateOf(Tab.STATION) }
    val start = remember { if (settings.onboarded) "main" else "welcome" }
    val dao = container.db.dao()

    val trips by dao.observeTrips().collectAsStateWithLifecycle(initialValue = null)
    val moments by dao.observeMoments().collectAsStateWithLifecycle(initialValue = null)
    val notes by dao.observeNotes().collectAsStateWithLifecycle(initialValue = null)
    val depot = remember(trips, moments, notes, settings) {
        Depot(trips.orEmpty(), moments.orEmpty(), notes.orEmpty(), settings, container.deck)
    }
    val go = remember(nav) { Go(nav) }

    // Onboarded elsewhere (a restored backup, the demo seed): leave the welcome screens.
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(settings.onboarded, route) {
        if (settings.onboarded && route == "welcome") nav.navigate("main") { popUpTo("welcome") { inclusive = true } }
    }

    Box(Modifier.fillMaxSize().background(U.c.bg)) {
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.background(U.c.bg),
            enterTransition = { fadeIn(tween(260)) },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(260)) },
            popExitTransition = { fadeOut(tween(180)) },
        ) {
            composable("welcome") {
                OnboardingScreen(settings) { a, b, seating ->
                    container.write { this.settings.update { it.copy(onboarded = true, nameA = a, nameB = b, seating = seating) } }
                    nav.navigate("main") { popUpTo("welcome") { inclusive = true } }
                }
            }
            composable("main") { MainTabs(tab, { tab = it }, go, depot) }
            composable("board?to={to}", arguments = listOf(navArgument("to") { type = NavType.StringType; defaultValue = "" })) { e ->
                BoardScreen(depot, go, e.arguments?.getString("to").orEmpty())
            }
            composable("trip/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                TripScreen(e.arguments?.getLong("id") ?: 0L, depot, go)
            }
            composable("ticket/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                TicketScreen(e.arguments?.getLong("id") ?: 0L, depot, go)
            }
            composable("settings") { SettingsScreen(depot, go) }
        }
    }
}

@Composable
private fun MainTabs(tab: Tab, onTab: (Tab) -> Unit, go: Go, depot: Depot) {
    Column(Modifier.fillMaxSize().background(U.c.bg)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                Tab.STATION -> StationTab(depot, go) { onTab(Tab.MAP) }
                Tab.MAP -> MapTab(depot, go)
                Tab.TICKETS -> TicketsTab(depot, go)
                Tab.ALBUM -> AlbumTab(depot, go)
            }
        }
        HorizontalDivider(color = U.c.line)
        Row(
            Modifier.fillMaxWidth().background(U.c.surface).navigationBarsPadding().height(66.dp).padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(Icons.Rounded.Train, stringResource(R.string.tab_station), tab == Tab.STATION) { onTab(Tab.STATION) }
            NavItem(Icons.Rounded.Map, stringResource(R.string.tab_map), tab == Tab.MAP) { onTab(Tab.MAP) }
            NavItem(Icons.Rounded.ConfirmationNumber, stringResource(R.string.tab_tickets), tab == Tab.TICKETS) { onTab(Tab.TICKETS) }
            NavItem(Icons.Rounded.AutoStories, stringResource(R.string.tab_album), tab == Tab.ALBUM) { onTab(Tab.ALBUM) }
        }
    }
}

@Composable
private fun RowScope.NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) U.c.brass else U.c.faint
    Column(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.clip(RoundedCornerShape(50)).background(if (selected) U.c.brassSoft else Color.Transparent).padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, Modifier.size(22.dp), tint = color) }
        Spacer(Modifier.height(3.dp))
        Text(label, fontFamily = Sans, fontSize = 12.5.sp, color = color, maxLines = 1)
    }
}
