package com.example.movie_project.views.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movie_project.R
import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.views.theme.DarkGrey
import com.example.movie_project.views.theme.Iris
import com.example.movie_project.views.theme.MovieMagicTheme
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DetailRoute(
    onNavigateBack: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val movie = uiState.movie

    when {
        movie != null -> DetailScreen(
            movie = movie,
            isFavorite = uiState.isFavorite,
            onNavigateBack = onNavigateBack,
            onProfileClick = onProfileClick,
            onFavoriteToggle = viewModel::toggleFavorite
        )
        uiState.error != null -> DetailErrorState(
            message = uiState.error!!,
            onRetry = viewModel::load,
            onNavigateBack = onNavigateBack
        )
        else -> DetailLoadingState()
    }
}

@Composable
private fun DetailLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Iris)
    }
}

@Composable
private fun DetailErrorState(
    message: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = DarkGrey)
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Iris)
        ) {
            Text("Retry")
        }
        Spacer(modifier = Modifier.size(8.dp))
        TextButton(onClick = onNavigateBack) {
            Text("Go back", color = Iris)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScreen(
    movie: MovieModel,
    isFavorite: Boolean,
    onNavigateBack: () -> Unit,
    onProfileClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val formattedDate = try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val output = SimpleDateFormat("MM-dd-yyyy", Locale.US)
        output.format(input.parse(movie.releaseDate ?: "") ?: "")
    } catch (e: Exception) {
        movie.releaseDate ?: "N/A"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = movie.title ?: "",
                        color = Iris,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.round_account_circle_24),
                            contentDescription = "Profile",
                            tint = Iris
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Poster card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.posterUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.placeholder2)
                        .error(R.drawable.placeholder2)
                        .build(),
                    contentDescription = movie.title,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Title
            Text(
                text = movie.title ?: "",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DarkGrey,
                modifier = Modifier.padding(8.dp)
            )

            // Release date + favorite button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formattedDate,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkGrey,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Iris else DarkGrey,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Overview
            Text(
                text = movie.overview ?: "",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DarkGrey,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetailScreenPreview() {
    MovieMagicTheme {
        DetailScreen(
            movie = MovieModel(
                id = 1,
                title = "Inception",
                overview = "A thief who steals corporate secrets through dream-sharing technology.",
                posterPath = "/sample.jpg",
                releaseDate = "2010-07-16"
            ),
            isFavorite = false,
            onNavigateBack = {},
            onProfileClick = {},
            onFavoriteToggle = {}
        )
    }
}
