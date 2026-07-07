package com.example.movie_project.views.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.movie_project.R
import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.views.HomeViewModel
import com.example.movie_project.views.components.MovieCard
import com.example.movie_project.views.theme.Iris
import com.example.movie_project.views.theme.MovieMagicTheme

@Composable
fun HomeRoute(
    onMovieClick: (MovieModel) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        movies = uiState.movies,
        isLoading = uiState.isLoading,
        onMovieClick = onMovieClick,
        onProfileClick = onProfileClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    movies: List<MovieModel>,
    isLoading: Boolean,
    onMovieClick: (MovieModel) -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Popular Movies") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            painter = painterResource(R.drawable.round_account_circle_24),
                            contentDescription = "Profile",
                            tint = Iris
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Iris
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(movies) { movie ->
                        MovieCard(movie = movie, onClick = onMovieClick)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    MovieMagicTheme {
        HomeScreen(
            movies = listOf(
                MovieModel(id = 1, title = "Movie 1", poster_path = "/sample.jpg"),
                MovieModel(id = 2, title = "Movie 2", poster_path = "/sample.jpg"),
                MovieModel(id = 3, title = "Movie 3", poster_path = "/sample.jpg"),
                MovieModel(id = 4, title = "Movie 4", poster_path = "/sample.jpg")
            ),
            isLoading = false,
            onMovieClick = {},
            onProfileClick = {}
        )
    }
}
