package com.example.movie_project.views.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movie_project.R
import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.views.auth.LoginRoute
import com.example.movie_project.views.auth.SignUpRoute
import com.example.movie_project.views.detail.DetailRoute
import com.example.movie_project.views.favorites.FavoritesRoute
import com.example.movie_project.views.home.HomeRoute
import com.example.movie_project.views.movielog.MovieLogDetailRoute
import com.example.movie_project.views.movielog.MovieLogRoute
import com.example.movie_project.views.profile.ProfileRoute
import com.example.movie_project.views.search.SearchRoute
import com.example.movie_project.views.search.SearchViewModel
import com.example.movie_project.views.theme.Iris
import com.example.movie_project.views.users.UsersRoute

private val bottomNavRoutes = setOf(
    Route.HOME, Route.FAVORITES, Route.SEARCH, Route.MOVIE_LOG
)

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: Int,
    val unselectedIcon: Int
)

private val bottomNavItems = listOf(
    BottomNavItem(Route.HOME, "Home", R.drawable.filled_home_24, R.drawable.outline_home),
    BottomNavItem(Route.FAVORITES, "Favorites", R.drawable.baseline_favorite_24, R.drawable.round_favorite_border_24),
    BottomNavItem(Route.SEARCH, "Search", R.drawable.filled_search_24, R.drawable.outline_search),
    BottomNavItem(Route.MOVIE_LOG, "Log", R.drawable.filled_book_24, R.drawable.book_24)
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                AppBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth
            composable(Route.LOGIN) {
                LoginRoute(
                    onNavigateToMain = {
                        navController.navigate(Route.HOME) {
                            popUpTo(Route.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = { navController.navigate(Route.SIGN_UP) }
                )
            }

            composable(Route.SIGN_UP) {
                SignUpRoute(
                    onNavigateToMain = {
                        navController.navigate(Route.HOME) {
                            popUpTo(Route.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // Bottom nav tabs
            composable(Route.HOME) {
                HomeRoute(
                    onMovieClick = { movie -> navController.navigate(Route.detail(movie)) },
                    onProfileClick = { navController.navigate(Route.PROFILE) }
                )
            }

            composable(Route.FAVORITES) {
                FavoritesRoute(
                    onMovieClick = { movie -> navController.navigate(Route.detail(movie)) },
                    onProfileClick = { navController.navigate(Route.PROFILE) }
                )
            }

            composable(Route.SEARCH) {
                val searchViewModel = hiltViewModel<SearchViewModel>()
                SearchRoute(
                    viewModel = searchViewModel,
                    onMovieClicked = { movie -> navController.navigate(Route.detail(movie)) },
                    onProfileClicked = { navController.navigate(Route.PROFILE) }
                )
            }

            composable(Route.MOVIE_LOG) {
                MovieLogRoute(
                    onAddEntry = { navController.navigate(Route.movieLogDetail()) },
                    onEntryClick = { entry -> navController.navigate(Route.movieLogDetail(entry.entryId)) }
                )
            }

            // Detail screen
            composable(
                route = Route.DETAIL,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                    navArgument("posterPath") { type = NavType.StringType; defaultValue = "" },
                    navArgument("overview") { type = NavType.StringType; defaultValue = "" },
                    navArgument("releaseDate") { type = NavType.StringType; defaultValue = "" },
                    navArgument("voteAverage") { type = NavType.FloatType; defaultValue = 0f }
                )
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val movie = MovieModel(
                    id = args?.getInt("movieId") ?: 0,
                    title = args?.getString("title"),
                    posterPath = args?.getString("posterPath"),
                    overview = args?.getString("overview"),
                    releaseDate = args?.getString("releaseDate"),
                    voteAverage = args?.getFloat("voteAverage")
                )
                DetailRoute(
                    movie = movie,
                    onNavigateBack = { navController.popBackStack() },
                    onProfileClick = { navController.navigate(Route.PROFILE) }
                )
            }

            // Movie Log Detail (add or edit)
            composable(
                route = Route.MOVIE_LOG_DETAIL,
                arguments = listOf(
                    navArgument("entryId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                MovieLogDetailRoute(
                    entryId = backStackEntry.arguments?.getString("entryId"),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Profile
            composable(Route.PROFILE) {
                ProfileRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onSignedOut = {
                        navController.navigate(Route.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Users
            composable(Route.USERS) {
                UsersRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onProfileClick = { navController.navigate(Route.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Route.HOME) { saveState = true }
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
