package com.example.eventconnect.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.eventconnect.models.Friend

@Composable
fun InvitationCard(friend: Friend, onAccept: () -> Unit, onDecline: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFF1C1C1E),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = friend.avatarUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(friend.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(friend.email, color = Color.Gray, fontSize = 12.sp)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDecline,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFFF6F61),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Decline")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF66BB6A),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
fun FriendCard(friend: Friend) {
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFF1C1C1E),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = friend.avatarUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(friend.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(friend.email, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
@Preview
@Composable
fun InvitationCardPreview() {
    val friend = Friend("John Doe", "john.mclean@examplepetstore.com", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d")
    Column {
        InvitationCard(friend = friend, onAccept = {}, onDecline = {})
        FriendCard(friend = friend)
    }
}
