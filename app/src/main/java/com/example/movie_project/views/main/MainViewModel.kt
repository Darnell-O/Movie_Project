package com.example.movie_project.views.main

import androidx.lifecycle.ViewModel
import com.example.movie_project.data.repository.AuthRepository
import com.example.movie_project.views.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Drives top-level, auth-dependent navigation:
 * - [startDestination] reflects whether a user is already signed in at cold start.
 * - [authState] lets the nav host redirect to Login whenever the user signs out
 *   from anywhere in the app.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val startDestination: Route =
        if (authRepository.isSignedIn) Route.Home else Route.Login

    val authState: Flow<String?> = authRepository.authState()
}