package com.example.movie_project.views.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.movie_project.R
import com.example.movie_project.views.auth.LoginRoute
import com.example.movie_project.views.auth.SignUpRoute
import com.example.movie_project.views.detail.DetailRoute
import com.example.movie_project.views.favorites.FavoritesRoute
import com.example.movie_project.views.home.HomeRoute
import com.example.movie_project.views.main.MainViewModel
import com.example.movie_project.views.movielog.MovieLogDetailRoute
import com.example.movie_project.views.movielog.MovieLogRoute
import com.example.movie_project.views.profile.ProfileRoute
import com.example.movie_project.views.search.SearchRoute
import com.example.movie_project.views.search.SearchViewModel
import com.example.movie_project.views.theme.Iris
import com.example.movie_project.views.users.UsersRoute

private data class BottomNavItem(
    val route: Route,
    val label: String,
    val selectedIcon: Int,
    val unselectedIcon: Int
)

private val bottomNavItems = listOf(
    BottomNavItem(Route.Home, "Home", R.drawable.filled_home_24, R.drawable.outline_home),
    BottomNavItem(Route.Favorites, "Favorites", R.drawable.baseline_favorite_24, R.drawable.round_favorite_border_24),
    BottomNavItem(Route.Search, "Search", R.drawable.filled_search_24, R.drawable.outline_search),
    BottomNavItem(Route.MovieLog, "Log", R.drawable.filled_book_24, R.drawable.book_24)
)

@Composable
fun AppNavHost(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    // Redirect to Login whenever the user signs out from anywhere in the app.
    LaunchedEffect(Unit) {
        viewModel.authState.collect { uid ->
            if (uid == null) {
                navController.navigate(Route.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(navController = navController, currentDestination = currentDestination)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = viewModel.startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth
            composable<Route.Login> {
                LoginRoute(
                    onNavigateToMain = {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = { navController.navigate(Route.SignUp) }
                )
            }

            composable<Route.SignUp> {
                SignUpRoute(
                    onNavigateToMain = {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // Bottom nav tabs
            composable<Route.Home> {
                HomeRoute(
                    onMovieClick = { movie -> navController.navigate(Route.Detail(movie.id)) },
                    onProfileClick = { navController.navigate(Route.Profile) }
                )
            }

            composable<Route.Favorites> {
                FavoritesRoute(
                    onMovieClick = { movie -> navController.navigate(Route.Detail(movie.id)) },
                    onProfileClick = { navController.navigate(Route.Profile) }
                )
            }

            composable<Route.Search> {
                val searchViewModel = hiltViewModel<SearchViewModel>()
                SearchRoute(
                    viewModel = searchViewModel,
                    onMovieClicked = { movie -> navController.navigate(Route.Detail(movie.id)) },
                    onProfileClicked = { navController.navigate(Route.Profile) }
                )
            }

            composable<Route.MovieLog> {
                MovieLogRoute(
                    onAddEntry = { navController.navigate(Route.MovieLogDetail()) },
                    onEntryClick = { entry -> navController.navigate(Route.MovieLogDetail(entry.entryId)) }
                )
            }

            // Detail screen
            composable<Route.Detail> {
                DetailRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onProfileClick = { navController.navigate(Route.Profile) }
                )
            }

            // Movie Log Detail (add or edit)
            composable<Route.MovieLogDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.MovieLogDetail>()
                MovieLogDetailRoute(
                    entryId = args.entryId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Profile
            composable<Route.Profile> {
                ProfileRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Users
            composable<Route.Users> {
                UsersRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onProfileClick = { navController.navigate(Route.Profile) }
                )
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavController, currentDestination: NavDestination?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Route.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            if (selected) item.selectedIcon else item.unselectedIcon
                        ),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Iris,
                    selectedTextColor = Iris,
                    indicatorColor = Iris.copy(alpha = 0.12f)
                )
            )
        }
    }
}