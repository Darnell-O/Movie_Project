package com.example.movie_project.views.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes. Each destination is a [Serializable] type consumed
 * by the Navigation-Compose type-safe API (composable<Route.X> / navigate(Route.X)).
 */
sealed interface Route {

    @Serializable data object Login : Route
    @Serializable data object SignUp : Route

    @Serializable data object Home : Route
    @Serializable data object Favorites : Route
    @Serializable data object Search : Route
    @Serializable data object MovieLog : Route

    @Serializable data object Profile : Route
    @Serializable data object Users : Route

    /** Movie detail. Carries only the id; DetailViewModel loads the rest. */
    @Serializable
    data class Detail(val movieId: Int) : Route

    /** Movie log entry (null entryId = new entry). */
    @Serializable
    data class MovieLogDetail(val entryId: String? = null) : Route
}