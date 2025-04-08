package com.example.eventconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.ContextThemeWrapper
import java.util.Calendar
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventconnect.ui.theme.blue

@Composable
fun AddEventScreen() {
    var eventName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("18/03/2025") }
    var selectedTime by remember { mutableStateOf("18:30") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Event",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Handle photo upload */ },
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF007BFF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Photo", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color.White,
                focusedLabelColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color.White,
                focusedLabelColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current
        val calendar = remember { Calendar.getInstance() }

        var selectedDate by remember { mutableStateOf("18/03/2025") }
        var selectedTime by remember { mutableStateOf("18:30") }

        Text(text = "Date:", color = Color.White, fontSize = 16.sp)

        Button(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                        selectedDate = formattedDate
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = blue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(selectedDate, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Time:", color = Color.White, fontSize = 16.sp)

        Button(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                        selectedTime = formattedTime
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = blue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(selectedTime, color = Color.White)
        }

    }
}
