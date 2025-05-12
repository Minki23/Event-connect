package com.example.eventconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.eventconnect.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserScreen(onLogout: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val currentUser = FirebaseAuth.getInstance().currentUser
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.onBackground)
            }
        } else if (currentUser != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display user photo
                val photoUrl = currentUser.photoUrl?.toString()
                val painter = rememberAsyncImagePainter(
                    model = photoUrl,
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

                // Display username
                Text(
                    text = currentUser.displayName ?: "User",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = colors.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Display user email
                currentUser.email?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = colors.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text("Log Out")
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Please sign in to continue",
                    color = colors.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}