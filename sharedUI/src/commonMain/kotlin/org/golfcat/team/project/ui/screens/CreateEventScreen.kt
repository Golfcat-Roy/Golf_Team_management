package org.golfcat.team.project.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.golfcat.team.project.TeamRepository

@Composable
fun CreateEventScreen(
    teamId: String,
    repository: TeamRepository,
    onEventCreated: () -> Unit,
    onBack: () -> Unit,
    eventId: String? = null
) {
    Text("建立賽事畫面 (暫時停用)")
}
