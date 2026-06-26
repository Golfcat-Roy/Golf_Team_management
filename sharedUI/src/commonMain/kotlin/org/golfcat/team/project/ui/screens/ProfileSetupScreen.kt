package org.golfcat.team.project.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.golfcat.team.project.TeamRepository

@Composable
fun ProfileSetupScreen(repository: TeamRepository, onProfileUpdated: () -> Unit) {
    Text("個人資料設定 (暫時停用)")
}
