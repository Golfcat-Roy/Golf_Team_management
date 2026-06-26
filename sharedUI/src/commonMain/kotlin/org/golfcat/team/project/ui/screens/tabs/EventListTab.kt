package org.golfcat.team.project.ui.screens.tabs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.golfcat.team.project.TeamRepository

@Composable
fun EventListTab(
    teamId: String,
    repository: TeamRepository,
    onEventClick: (String) -> Unit,
    onAddEvent: () -> Unit,
    onEditEvent: (String) -> Unit,
    onViewGroup: (String) -> Unit,
    onViewLeaderboard: (String) -> Unit
) {
    Text("賽事列表 (暫時停用)")
}
