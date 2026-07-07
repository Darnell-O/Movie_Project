package com.example.movie_project.views.users

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.movie_project.models.domain.UsersModel
import com.example.movie_project.views.theme.DarkGrey
import com.example.movie_project.views.theme.Iris
import com.example.movie_project.views.theme.LightWhite
import com.example.movie_project.views.theme.MovieMagicTheme

@Composable
fun UsersRoute(
    onNavigateBack: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: UsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UsersScreen(
        users = uiState.users,
        isLoading = uiState.isLoading,
        onNavigateBack = onNavigateBack,
        onProfileClick = onProfileClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsersScreen(
    users: List<UsersModel>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Users",
                        color = Iris,
                        fontWeight = FontWeight.Bold
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
                            painter = androidx.compose.ui.res.painterResource(
                                com.example.movie_project.R.drawable.round_account_circle_24
                            ),
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(users) { user ->
                        UserItem(user = user)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserItem(user: UsersModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = LightWhite)
    ) {
        Text(
            text = user.email ?: "",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGrey,
            modifier = Modifier.padding(start = 16.dp, top = 5.dp, bottom = 5.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun UsersScreenPreview() {
    MovieMagicTheme {
        UsersScreen(
            users = listOf(
                UsersModel(email = "john@example.com", uid = "1"),
                UsersModel(email = "jane@example.com", uid = "2"),
                UsersModel(email = "bob@example.com", uid = "3")
            ),
            isLoading = false,
            onNavigateBack = {},
            onProfileClick = {}
        )
    }
}
