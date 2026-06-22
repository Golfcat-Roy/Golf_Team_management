package org.golfcat.team.project.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.*
import org.golfcat.team.project.models.*

@Composable
fun LeaderboardTab(teamId: String) {
    val repository = remember { TeamRepository() }
    var members by remember { mutableStateOf<List<MemberWithUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(teamId) {
        isLoading = true
        try {
            members = repository.getLeaderboard(teamId)
        } catch (e: Exception) {
            println("LeaderboardTab load error: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Text("球隊排行榜", style = MaterialTheme.typography.headlineSmall)
                Text("依差點排序 (由小到大)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(16.dp))
            }
            itemsIndexed(members, key = { _, member -> member.id }) { index, member ->
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
                        val rankColor = when (index) {
                            0 -> androidx.compose.ui.graphics.Color(0xFFFFD700)
                            1 -> androidx.compose.ui.graphics.Color(0xFFC0C0C0)
                            2 -> androidx.compose.ui.graphics.Color(0xFFCD7F32)
                            else -> MaterialTheme.colorScheme.outline
                        }
                        
                        Box(
                            modifier = Modifier.width(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = rankColor
                            )
                        }

                        Column(Modifier.weight(1f)) {
                            Text(
                                member.users.realName, 
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "職務: ${MemberRoles.getDisplayName(member.role)}", 
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("目前差點", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = member.handicap.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
