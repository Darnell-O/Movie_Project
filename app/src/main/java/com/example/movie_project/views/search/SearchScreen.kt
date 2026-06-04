package com.example.movie_project.views.search

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movie_project.R
import com.example.movie_project.models.MovieModel
import com.example.movie_project.util.HapticUtil
import com.example.movie_project.views.theme.DarkGrey
import com.example.movie_project.views.theme.Iris
import com.example.movie_project.views.theme.LightWhite
import com.example.movie_project.views.theme.MovieMagicTheme

/**
 * Stateful entry point that binds the [SearchViewModel] to the stateless
 * [SearchScreen]. This thin wrapper is the only composable that knows about the
 * ViewModel — keeping the actual UI ([SearchScreen]) stateless and previewable.
 */
@Composable
fun SearchRoute(
    viewModel: SearchViewModel,
    onMovieClicked: (MovieModel) -> Unit,
    onProfileClicked: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Surface errors as a transient toast, then notify the VM it was consumed.
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onErrorShown()
        }
    }

    SearchScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onMovieClicked = onMovieClicked,
        onProfileClicked = onProfileClicked,
    )
}

/**
 * Stateless Search UI. All state is hoisted in via [uiState] and all events are
 * emitted via the lambdas, so this composable is fully previewable and testable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onMovieClicked: (MovieModel) -> Unit,
    onProfileClicked: () -> Unit,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightWhite)
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.search),
                        color = Iris,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            actions = {
                AsyncImage(
                    model = R.drawable.bw_stockphoto3,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onProfileClicked() }
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = LightWhite)
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Search Movies") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (uiState.query.isBlank()) {
                        Toast.makeText(
                            context,
                            "Please enter a search query",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        onSearch()
                        keyboardController?.hide()
                    }
                }
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightWhite),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isEmpty) {
                Text(
                    text = stringResource(R.string.begin_movie_search),
                    color = DarkGrey,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 130.dp)
                )
            } else {
                MovieGrid(movies = uiState.movies, onMovieClicked = onMovieClicked)
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(color = Iris)
            }
        }
    }
}

@Composable
private fun MovieGrid(
    movies: List<MovieModel>,
    onMovieClicked: (MovieModel) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 80.dp),
    ) {
        items(movies, key = { it.id }) { movie ->
            MovieCard(movie = movie, onMovieClicked = onMovieClicked)
        }
    }
}

@Composable
private fun MovieCard(
    movie: MovieModel,
    onMovieClicked: (MovieModel) -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current
    val posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                HapticUtil.performClickFeedback(view)
                onMovieClicked(movie)
            },
        shape = RoundedCornerShape(8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(posterUrl)
                .crossfade(true)
                .placeholder(R.drawable.placeholder2)
                .error(R.drawable.placeholder2)
                .build(),
            contentDescription = movie.title,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenEmptyPreview() {
    MovieMagicTheme {
        SearchScreen(
            uiState = SearchUiState(),
            onQueryChange = {},
            onSearch = {},
            onMovieClicked = {},
            onProfileClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenResultsPreview() {
    MovieMagicTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "Spider",
                movies = listOf(
                    MovieModel(id = 1, title = "Spider-Man"),
                    MovieModel(id = 2, title = "Spider-Verse"),
                )
            ),
            onQueryChange = {},
            onSearch = {},
            onMovieClicked = {},
            onProfileClicked = {},
        )
    }
}