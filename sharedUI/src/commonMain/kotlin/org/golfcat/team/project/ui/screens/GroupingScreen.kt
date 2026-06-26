package org.golfcat.team.project.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.golfcat.team.project.TeamRepository

@Composable
fun GroupingScreen(
    eventId: String,
    repository: TeamRepository,
    onBack: () -> Unit
) {
    Text("分組畫面 (暫時停用)")
}
