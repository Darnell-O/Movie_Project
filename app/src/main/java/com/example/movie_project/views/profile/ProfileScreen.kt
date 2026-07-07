package com.example.movie_project.views.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.movie_project.R
import com.example.movie_project.views.theme.DarkGrey
import com.example.movie_project.views.theme.Iris
import com.example.movie_project.views.theme.LightWhite
import com.example.movie_project.views.theme.MovieMagicTheme

private val VeryLightPurple = Color(0xFFF3F0FB)

@Composable
fun ProfileRoute(
    onNavigateBack: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            viewModel.onNavigatedToLogin()
            onSignedOut()
        }
    }

    ProfileScreen(
        userEmail = uiState.userEmail,
        isLoading = uiState.isLoading,
        onNavigateBack = onNavigateBack,
        onSignOut = viewModel::signOut
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    userEmail: String?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.ic_notification),
                            contentDescription = "Notifications",
                            tint = DarkGrey
                        )
                    }
                }
            )
        },
        containerColor = VeryLightPurple
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile card with avatar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Card sits below the avatar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 75.dp),
                    shape = RoundedCornerShape(15.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = LightWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Name",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrey
                        )
                        Text(
                            text = "User name",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = DarkGrey,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        HorizontalDivider(color = Iris, thickness = 1.dp)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Info", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGrey)
                            Box(modifier = Modifier.width(1.dp).height(56.dp).background(Iris))
                            Text("Twitter", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGrey)
                            Box(modifier = Modifier.width(1.dp).height(56.dp).background(Iris))
                            Text("Instagram", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGrey)
                        }
                    }
                }

                // Circular avatar overlapping the card
                Image(
                    painter = painterResource(R.drawable.bw_stockphoto3),
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopCenter)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_email),
                            contentDescription = "Email",
                            modifier = Modifier.size(30.dp),
                            tint = DarkGrey
                        )
                        Text(
                            text = "Email",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DarkGrey,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                    Text(
                        text = userEmail ?: "—",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkGrey,
                        modifier = Modifier.padding(start = 25.dp, bottom = 8.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSignOut,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Iris)
            ) {
                Text(if (isLoading) "Signing out…" else "Logout")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    MovieMagicTheme {
        ProfileScreen(
            userEmail = "user@example.com",
            isLoading = false,
            onNavigateBack = {},
            onSignOut = {}
        )
    }
}
