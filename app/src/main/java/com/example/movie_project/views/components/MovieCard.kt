package com.example.movie_project.views.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movie_project.R
import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.views.theme.LightWhite
import com.example.movie_project.views.theme.MovieMagicTheme

@Composable
fun MovieCard(
    movie: MovieModel,
    onClick: (MovieModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick(movie) },
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = LightWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://image.tmdb.org/t/p/w500${movie.poster_path}")
                .crossfade(true)
                .placeholder(R.drawable.placeholder2)
                .error(R.drawable.placeholder2)
                .build(),
            contentDescription = movie.title,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieCardPreview() {
    MovieMagicTheme {
        MovieCard(
            movie = MovieModel(
                id = 1,
                title = "Sample Movie",
                poster_path = "/sample.jpg"
            ),
            onClick = {}
        )
    }
}
