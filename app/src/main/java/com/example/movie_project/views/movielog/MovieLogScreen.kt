package com.example.movie_project.views.movielog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.views.theme.Iris
import com.example.movie_project.views.theme.MovieMagicTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MovieLogRoute(
    onAddEntry: () -> Unit,
    onEntryClick: (MovieLogEntry) -> Unit,
    viewModel: MovieLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MovieLogScreen(
        entries = uiState.entries,
        isLoading = uiState.isLoading,
        onAddEntry = onAddEntry,
        onEntryClick = onEntryClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieLogScreen(
    entries: List<MovieLogEntry>,
    isLoading: Boolean,
    onAddEntry: () -> Unit,
    onEntryClick: (MovieLogEntry) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Movie Log") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEntry,
                containerColor = Iris
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add entry", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Iris
                    )
                }
                entries.isEmpty() -> {
                    Text(
                        text = "No movies logged yet.\nTap + to add your first entry.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(entries) { entry ->
                            MovieLogItem(entry = entry, onClick = onEntryClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieLogItem(
    entry: MovieLogEntry,
    onClick: (MovieLogEntry) -> Unit
) {
    val dateFormatted = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        .format(Date(entry.dateAdded))

    Card(
        onClick = { onClick(entry) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = entry.movieTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Added: $dateFormatted",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MovieLogScreenPreview() {
    MovieMagicTheme {
        MovieLogScreen(
            entries = listOf(
                MovieLogEntry(movieTitle = "The Shawshank Redemption", dateAdded = System.currentTimeMillis()),
                MovieLogEntry(movieTitle = "The Godfather", dateAdded = System.currentTimeMillis()),
                MovieLogEntry(movieTitle = "Inception", dateAdded = System.currentTimeMillis())
            ),
            isLoading = false,
            onAddEntry = {},
            onEntryClick = {}
        )
    }
}
