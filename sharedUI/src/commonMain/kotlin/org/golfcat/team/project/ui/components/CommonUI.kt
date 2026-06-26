package org.golfcat.team.project.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ErrorMessage(message: String) {
    Text("錯誤: $message")
}

@Composable
fun LoadingSpinner() {
    Text("載入中...")
}
