package io.github.linxiks.pindex.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.ui.ability.AbilityDetailScreen
import io.github.linxiks.pindex.ui.ability.AbilityDetailViewModel
import io.github.linxiks.pindex.ui.favorites.FavoritesScreen
import io.github.linxiks.pindex.ui.move.MoveDetailScreen
import io.github.linxiks.pindex.ui.move.MoveDetailViewModel
import io.github.linxiks.pindex.ui.pokedex.PokedexScreen
import io.github.linxiks.pindex.ui.pokemon.PokemonDetailScreen
import io.github.linxiks.pindex.ui.pokemon.PokemonDetailViewModel
import io.github.linxiks.pindex.ui.search.SearchScreen
import io.github.linxiks.pindex.ui.settings.SettingsScreen

private enum class TopLevel(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    Pokedex("pokedex", R.string.nav_pokedex, Icons.AutoMirrored.Filled.List),
    Search("search", R.string.nav_search, Icons.Filled.Search),
    Favorites("favorites", R.string.nav_favorites, Icons.Filled.Star),
    Settings("settings", R.string.nav_settings, Icons.Filled.Settings),
}

private const val ROUTE_POKEMON = "pokemon/{${PokemonDetailViewModel.ARG_POKEMON_ID}}"
private const val ROUTE_ABILITY = "ability/{${AbilityDetailViewModel.ARG_ABILITY_ID}}"
private const val ROUTE_MOVE = "move/{${MoveDetailViewModel.ARG_MOVE_ID}}"

private fun pokemonRoute(pokemonId: Int) = "pokemon/$pokemonId"
private fun abilityRoute(abilityId: Int) = "ability/$abilityId"
private fun moveRoute(moveId: Int) = "move/$moveId"

/** Tab switch that keeps each tab's back stack, ViewModels and scroll state. */
private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun PindexNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val topLevelRoutes = TopLevel.entries.map { it.route }
    val showBottomBar = destination?.route in topLevelRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevel.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = { navController.navigateTopLevel(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevel.Pokedex.route,
            // Consume so screens' own statusBarsPadding/TopAppBar insets do not double up.
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            // Every open pushes, so pokemon → move → pokemon unwinds one level per back.
            val openPokemon: (Int) -> Unit = { navController.navigate(pokemonRoute(it)) }
            val openAbility: (Int) -> Unit = { navController.navigate(abilityRoute(it)) }
            val openMove: (Int) -> Unit = { navController.navigate(moveRoute(it)) }
            composable(TopLevel.Pokedex.route) {
                PokedexScreen(
                    onOpenSearch = { navController.navigateTopLevel(TopLevel.Search.route) },
                    onOpenPokemon = openPokemon,
                )
            }
            composable(TopLevel.Search.route) {
                SearchScreen(
                    onBack = { navController.navigateUp() },
                    onOpenPokemon = openPokemon,
                    onOpenMove = openMove,
                    onOpenAbility = openAbility,
                )
            }
            composable(TopLevel.Favorites.route) { FavoritesScreen() }
            composable(TopLevel.Settings.route) { SettingsScreen() }
            composable(
                route = ROUTE_POKEMON,
                arguments = listOf(navArgument(PokemonDetailViewModel.ARG_POKEMON_ID) { type = NavType.IntType }),
            ) {
                PokemonDetailScreen(
                    onBack = { navController.navigateUp() },
                    onOpenPokemon = openPokemon,
                    onOpenAbility = openAbility,
                    onOpenMove = openMove,
                )
            }
            composable(
                route = ROUTE_ABILITY,
                arguments = listOf(navArgument(AbilityDetailViewModel.ARG_ABILITY_ID) { type = NavType.IntType }),
            ) {
                AbilityDetailScreen(onBack = { navController.navigateUp() }, onOpenPokemon = openPokemon)
            }
            composable(
                route = ROUTE_MOVE,
                arguments = listOf(navArgument(MoveDetailViewModel.ARG_MOVE_ID) { type = NavType.IntType }),
            ) {
                MoveDetailScreen(onBack = { navController.navigateUp() }, onOpenPokemon = openPokemon)
            }
        }
    }
}
