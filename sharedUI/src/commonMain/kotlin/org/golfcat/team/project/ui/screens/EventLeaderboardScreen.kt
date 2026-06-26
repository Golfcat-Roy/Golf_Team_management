package org.golfcat.team.project.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.golfcat.team.project.TeamRepository

@Composable
fun EventLeaderboardScreen(
    eventId: String,
    teamId: String,
    repository: TeamRepository,
    onBack: () -> Unit
) {
    Text("排行榜畫面 (暫時停用)")
}
