package org.golfcat.team.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.models.*

@Composable
fun App(repository: TeamRepository = remember { TeamRepository() }) {
    val currentUser by AuthManager.currentUser.collectAsState()
    var events by remember { mutableStateOf<List<EventWithDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            isLoading = true
            // 💡 呼叫我們的 Mock Repository
            events = repository.getEventsWithDetails("team-1", currentUser!!.id!!)
            isLoading = false
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (currentUser == null) {
                Box(contentAlignment = Alignment.Center) {
                    Text("請先登入")
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "歡迎回來, ${currentUser!!.realName}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "今日賽事列表 (Mock Data)", style = MaterialTheme.typography.titleLarge)
                    
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        events.forEach { event ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = event.title, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "地點: ${event.location ?: "未定"}")
                                    Text(text = "日期: ${event.date}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
