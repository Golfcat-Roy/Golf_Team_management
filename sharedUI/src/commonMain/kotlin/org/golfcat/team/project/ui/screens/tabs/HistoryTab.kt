package org.golfcat.team.project.ui.screens.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.*
import org.golfcat.team.project.models.*
import org.golfcat.team.project.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun HistoryTab(
    teamId: String, 
    subscriptionType: String, 
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    onNavigateToLeaderboard: (String) -> Unit
) {
    val repository = remember { TeamRepository() }
    val currentUser by AuthManager.currentUser.collectAsState()
    var personalHistory by remember { mutableStateOf<PersonalHistory?>(null) }
    var teamHistory by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                currentUser?.id?.let { userId ->
                    personalHistory = repository.getFullPersonalHistory(userId, teamId)
                    teamHistory = repository.getTeamMatchHistory(teamId)
                }
            } catch (e: Exception) {
                println("HistoryTab load error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(teamId, currentUser) {
        loadData()
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { onTabChange(0) }, text = { Text("個人成績") })
                Tab(selected = selectedTab == 1, onClick = { onTabChange(1) }, text = { Text("球隊歷史") })
            }

            if (selectedTab == 0) {
                PersonalHistoryView(personalHistory)
            } else {
                Column(Modifier.fillMaxSize()) {
                    if (subscriptionType == "free") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Text(
                                "💡 目前為免費版，僅顯示最近 2 場賽事紀錄。升級 Pro 版可查看完整歷史。",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    TeamHistoryView(teamHistory, onNavigateToLeaderboard)
                }
            }
        }
    }
}

@Composable
fun PersonalHistoryView(history: PersonalHistory?) {
    if (history == null || history.entries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("尚無個人歷史成績資料")
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Text("個人生涯統計", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatCard("Birdies", history.birdieCount.toString(), androidx.compose.ui.graphics.Color(0xFF4CAF50))
                    StatCard("Eagles+", history.eagleCount.toString(), androidx.compose.ui.graphics.Color(0xFFFFC107))
                    StatCard("Pars", history.parCount.toString(), androidx.compose.ui.graphics.Color(0xFF2196F3))
                }
                
                Spacer(Modifier.height(24.dp))
                Text("個人賽事紀錄", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            
            items(history.entries) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.date, style = MaterialTheme.typography.bodySmall)
                            Text(entry.eventTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("總桿: ${entry.grossScore}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                entry.netScore?.let { net ->
                                    Spacer(Modifier.width(12.dp))
                                    Text("淨桿: $net", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                entry.holeScores.take(9).forEachIndexed { i, score ->
                                    ScoreBadge(score, entry.pars[i])
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                entry.holeScores.drop(9).forEachIndexed { i, score ->
                                    ScoreBadge(score, entry.pars[i + 9])
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamHistoryView(events: List<Event>, onNavigateToLeaderboard: (String) -> Unit) {
    if (events.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("尚無球隊歷史賽事資料")
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
            items(events, key = { it.id!! }) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onNavigateToLeaderboard(event.id!!) },
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(event.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Text(event.date, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("📍 地點: ${event.location}", style = MaterialTheme.typography.bodyMedium)
                        Text("🏆 賽制: ${if (event.handicapRule == "New_New_Peoria") "新新貝利亞" else "球隊差點"}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Text("點擊查看詳情成績 📊", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.width(100.dp).height(80.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
