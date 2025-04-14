package com.example.eventconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.eventconnect.R
import com.example.eventconnect.ui.theme.blue
import androidx.compose.ui.text.font.FontWeight
import com.example.eventconnect.ui.data.UserViewModel

@Composable
fun UserScreen(viewModel: UserViewModel = viewModel()) {
    // Fetch user details when the screen appears
    LaunchedEffect(Unit) {
        viewModel.fetchUser(1)
    }

    // Fill the screen with a gradient background for a cooler appearance
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C3E50),
                        Color(0xFF4CA1AF)
                    )
                )
            )
    ) {
        if (viewModel.user == null) {
            // Display a centered progress indicator while loading
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            val user = viewModel.user!!
            // Center all content in a Column that fills all space
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Load the user's avatar with a placeholder image
                val painter = rememberAsyncImagePainter(
                    model = user.avatarUrl,
                    placeholder = painterResource(id = R.mipmap.placeholder),
                    error = painterResource(id = R.mipmap.placeholder)
                )
                Image(
                    painter = painter,
                    contentDescription = "User Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Display username without any prefix label
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Display user email
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
