package org.golfcat.team.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.models.*

@Composable
fun App() {
    val currentUser by AuthManager.currentUser.collectAsState()
    
    val events = listOf(
        EventWithDetails(
            id = "e1", 
            title = "2024 Summer Open", // 💡 英文化
            date = "2024-06-25", 
            location = "Daxi Golf Course", // 💡 英文化
            registrationStatus = "closed", 
            handicapRule = "New_New_Peoria", 
            startTime = "08:00", 
            groupCount = 4, 
            participantCount = 16, 
            isUserRegistered = true, 
            isArchivedInList = false
        )
    )

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (currentUser == null) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Please Login")
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Welcome, ${currentUser?.realName ?: "User"}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Today's Events (English Mode)", style = MaterialTheme.typography.titleLarge)
                    
                    events.forEach { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = event.title, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Location: ${event.location ?: "TBD"}")
                                Text(text = "Date: ${event.date}")
                            }
                        }
                    }
                }
            }
        }
    }
}
