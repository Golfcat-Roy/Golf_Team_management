package org.golfcat.team.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.golfcat.team.project.models.EventWithDetails

@Composable
fun EventListTab(repository: TeamRepository) {
    val events by repository.events.collectAsState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(ResStrings.EVENT_LIST_TITLE, style = MaterialTheme.typography.headlineSmall)
            // 💡 Admin only (Mocked)
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(events) { event ->
                EventCard(
                    event = event,
                    onJoinClick = {
                        scope.launch { repository.toggleRegistration(event.id) }
                    }
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: EventWithDetails,
    onJoinClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StatusTag(event.registrationStatus)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            IconTextInfo(Icons.Default.Place, event.location ?: "TBD")
            IconTextInfo(Icons.Default.DateRange, "${event.date} ${event.startTime ?: ""}")
            IconTextInfo(Icons.Default.Person, "${ResStrings.EVENT_REGISTERED_COUNT}: ${event.participantCount}")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onJoinClick,
                    modifier = Modifier.weight(1f),
                    colors = if (event.isUserRegistered) 
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) 
                        else ButtonDefaults.buttonColors()
                ) {
                    Text(if (event.isUserRegistered) ResStrings.EVENT_CANCEL_JOIN else ResStrings.EVENT_JOIN)
                }
                
                // 💡 Manage Button (Always visible in Mock)
                OutlinedButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(ResStrings.EVENT_MANAGE)
                }
            }
        }
    }
}

@Composable
fun StatusTag(status: String) {
    val color = if (status == "open") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun IconTextInfo(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
