package org.golfcat.team.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
    var showGuestDialog by remember { mutableStateOf<String?>(null) } // eventId

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(ResStrings.EVENT_LIST_TITLE, style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { /* TODO: Create Event */ }) {
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
                    },
                    onAddGuestClick = {
                        showGuestDialog = event.id
                    }
                )
            }
        }
    }

    // 💡 Guest Registration Dialog
    if (showGuestDialog != null) {
        GuestDialog(
            onDismiss = { showGuestDialog = null },
            onConfirm = { name, hcp ->
                println("Add Guest: $name with HCP $hcp to ${showGuestDialog}")
                showGuestDialog = null
            }
        )
    }
}

@Composable
fun EventCard(
    event: EventWithDetails,
    onJoinClick: () -> Unit,
    onAddGuestClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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
            IconTextInfo(Icons.Default.Settings, "${ResStrings.EVENT_RULE}: ${event.handicapRule}")
            
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
                
                IconButton(onClick = onAddGuestClick) {
                    Icon(Icons.Default.Person, contentDescription = ResStrings.EVENT_GUEST_JOIN)
                }

                Box {
                    OutlinedButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(ResStrings.EVENT_MANAGE)
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(ResStrings.MENU_SCORING) },
                            onClick = { showMenu = false /* TODO */ },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(ResStrings.MENU_GROUPING) },
                            onClick = { showMenu = false /* TODO */ },
                            leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(ResStrings.MENU_LEADERBOARD) },
                            onClick = { showMenu = false /* TODO */ },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(ResStrings.MENU_DELETE) },
                            onClick = { showMenu = false /* TODO */ },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuestDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var hcp by remember { mutableStateOf("36") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ResStrings.DIALOG_GUEST_TITLE) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text(ResStrings.GUEST_NAME) })
                TextField(value = hcp, onValueChange = { hcp = it }, label = { Text(ResStrings.GUEST_HCP) })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, hcp) }) { Text(ResStrings.CONFIRM) }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text(ResStrings.CANCEL) }
        }
    )
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
