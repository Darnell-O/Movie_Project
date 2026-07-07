package com.example.movie_project.views.movielog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.views.theme.Iris
import com.example.movie_project.views.theme.MovieMagicTheme
import java.util.UUID

@Composable
fun MovieLogDetailRoute(
    entryId: String?,
    onNavigateBack: () -> Unit,
    viewModel: MovieLogDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(entryId) {
        entryId?.let { viewModel.loadEntry(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MovieLogDetailEvent.Saved, is MovieLogDetailEvent.Deleted -> onNavigateBack()
            }
        }
    }

    MovieLogDetailScreen(
        existingEntry = uiState.entry,
        entryId = entryId,
        onSave = { entry ->
            if (entryId != null) viewModel.updateEntry(entry)
            else viewModel.insertEntry(entry)
        },
        onDelete = { entryId?.let { viewModel.deleteEntry(it) } },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieLogDetailScreen(
    existingEntry: MovieLogEntry?,
    entryId: String?,
    onSave: (MovieLogEntry) -> Unit,
    onDelete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var movieTitle by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var dateWatched by rememberSaveable { mutableStateOf("") }
    var directedBy by rememberSaveable { mutableStateOf("") }
    var starring by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var inTheater by rememberSaveable { mutableStateOf(false) }
    var atHome by rememberSaveable { mutableStateOf(false) }
    var firstWatch by rememberSaveable { mutableStateOf(false) }
    var rewatch by rememberSaveable { mutableStateOf(false) }
    var alone by rememberSaveable { mutableStateOf(false) }
    var withSomeone by rememberSaveable { mutableStateOf(false) }
    var notes by rememberSaveable { mutableStateOf("") }
    var dateAdded by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    // Populate fields when editing an existing entry
    LaunchedEffect(existingEntry) {
        existingEntry?.let {
            movieTitle = it.movieTitle
            year = it.year
            dateWatched = it.dateWatched
            directedBy = it.directedBy
            starring = it.starring
            rating = it.rating
            inTheater = it.inTheater
            atHome = it.atHome
            firstWatch = it.firstWatch
            rewatch = it.rewatch
            alone = it.alone
            withSomeone = it.withSomeone
            notes = it.notes
            dateAdded = it.dateAdded
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movie Log", color = Iris) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Star Rating
            SectionLabel("Rating")
            StarRating(rating = rating, onRatingChange = { rating = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Text Fields
            OutlinedTextField(
                value = movieTitle,
                onValueChange = { movieTitle = it },
                label = { Text("Movie Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Year") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dateWatched,
                onValueChange = { dateWatched = it },
                label = { Text("Date Watched") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = directedBy,
                onValueChange = { directedBy = it },
                label = { Text("Directed By") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = starring,
                onValueChange = { starring = it },
                label = { Text("Starring") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Where
            SectionLabel("Where")
            CheckboxRow(
                items = listOf("In Theater" to inTheater, "At Home" to atHome),
                onCheckedChange = { index, checked ->
                    if (index == 0) inTheater = checked else atHome = checked
                }
            )

            // Watch Type
            SectionLabel("Watch Type")
            CheckboxRow(
                items = listOf("First Watch" to firstWatch, "Rewatch" to rewatch),
                onCheckedChange = { index, checked ->
                    if (index == 0) firstWatch = checked else rewatch = checked
                }
            )

            // Who
            SectionLabel("Who")
            CheckboxRow(
                items = listOf("Alone" to alone, "With Someone" to withSomeone),
                onCheckedChange = { index, checked ->
                    if (index == 0) alone = checked else withSomeone = checked
                }
            )

            // Notes
            SectionLabel("Notes")
            OutlinedTextField(
                value = notes,
                onValueChange = { if (it.length <= 140) notes = it },
                label = { Text("Notes") },
                minLines = 3,
                maxLines = 5,
                supportingText = { Text("${notes.length}/140") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (movieTitle.isNotBlank()) {
                        onSave(
                            MovieLogEntry(
                                userId = "",
                                entryId = entryId ?: UUID.randomUUID().toString(),
                                movieTitle = movieTitle,
                                year = year,
                                dateWatched = dateWatched,
                                directedBy = directedBy,
                                starring = starring,
                                rating = rating,
                                inTheater = inTheater,
                                atHome = atHome,
                                firstWatch = firstWatch,
                                rewatch = rewatch,
                                alone = alone,
                                withSomeone = withSomeone,
                                notes = notes,
                                dateAdded = dateAdded
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Iris)
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun StarRating(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            IconButton(
                onClick = { onRatingChange(i) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $i",
                    tint = if (i <= rating) Iris else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun CheckboxRow(
    items: List<Pair<String, Boolean>>,
    onCheckedChange: (Int, Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, (label, checked) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Checkbox(checked = checked, onCheckedChange = { onCheckedChange(index, it) })
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MovieLogDetailScreenPreview() {
    MovieMagicTheme {
        MovieLogDetailScreen(
            existingEntry = null,
            entryId = null,
            onSave = {},
            onDelete = {},
            onNavigateBack = {}
        )
    }
}
