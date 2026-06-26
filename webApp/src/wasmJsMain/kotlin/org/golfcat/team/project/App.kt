package org.golfcat.team.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun App() {
    val currentUser by AuthManager.currentUser.collectAsState()
    
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (currentUser == null) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Please Login (Web English Mode)")
                }
            } else {
                // 💡 使用恢復後的主導航架構
                MainAppShell()
            }
        }
    }
}
